"""
Extract raw sector data from an IPF disk image using capsimg + MFM decoding,
producing a raw .ST sector dump.

Usage:
    python tools/ipf_to_st.py research/assets/atarist/starquake.ipf [output.st]

Requires: capsimg library (libcapsimage.dylib) built from
    https://github.com/FrodeSolheim/capsimg
"""

import ctypes
import ctypes.util
import struct
import sys
import os

CAPS_MTRS = 5

class CapsTrackInfo(ctypes.Structure):
    _pack_ = 1
    _fields_ = [
        ("type", ctypes.c_uint32), ("cylinder", ctypes.c_uint32),
        ("head", ctypes.c_uint32), ("sectorcnt", ctypes.c_uint32),
        ("sectorsize", ctypes.c_uint32), ("trackcnt", ctypes.c_uint32),
        ("trackbuf", ctypes.POINTER(ctypes.c_ubyte)), ("tracklen", ctypes.c_uint32),
        ("trackdata", ctypes.POINTER(ctypes.c_ubyte) * CAPS_MTRS),
        ("tracksize", ctypes.c_uint32 * CAPS_MTRS),
        ("timelen", ctypes.c_uint32), ("timebuf", ctypes.POINTER(ctypes.c_uint32)),
    ]

class CapsImageInfo(ctypes.Structure):
    _pack_ = 1
    _fields_ = [
        ("type", ctypes.c_uint32), ("release", ctypes.c_uint32),
        ("revision", ctypes.c_uint32),
        ("mincylinder", ctypes.c_uint32), ("maxcylinder", ctypes.c_uint32),
        ("minhead", ctypes.c_uint32), ("maxhead", ctypes.c_uint32),
        ("crdt", ctypes.c_uint32 * 7), ("platform", ctypes.c_uint32 * 4),
    ]

DI_LOCK_INDEX = 1 << 0
DI_LOCK_ALIGN = 1 << 1


def find_capsimg():
    for path in [
        "/tmp/capsimg-fsuae/CAPSImg/libcapsimage.dylib",
        "/usr/local/lib/libcapsimage.dylib",
        "/opt/homebrew/lib/libcapsimage.dylib",
        "libcapsimage.so.5", "libcapsimage.so",
    ]:
        if os.path.exists(path):
            return path
    return ctypes.util.find_library("capsimage")


def decode_mfm_word(bits, bit_offset, total_bits):
    """Decode one MFM word (16 MFM bits -> 8 data bits) from a bitstream."""
    byte = 0
    for b in range(8):
        data_bit_pos = bit_offset + 1 + b * 2  # data bits at odd positions (1,3,5,7,9,11,13,15)
        if data_bit_pos < total_bits:
            bit_val = (bits >> (total_bits - 1 - data_bit_pos)) & 1
            if bit_val:
                byte |= (1 << (7 - b))
    return byte


def decode_mfm_bytes(bits, bit_offset, count, total_bits):
    """Decode count MFM words into count data bytes."""
    result = bytearray()
    pos = bit_offset
    for _ in range(count):
        result.append(decode_mfm_word(bits, pos, total_bits))
        pos += 16
    return bytes(result), pos


def decode_mfm_track(mfm_bytes, sector_size=512):
    """
    Decode an MFM track bitstream to extract sector data.
    Returns dict of {sector_number: bytes}.
    """
    total_bits = len(mfm_bytes) * 8
    bits = int.from_bytes(mfm_bytes, 'big')
    sync_pattern = 0x4489
    mask = 0xFFFF

    # Find all triple-sync positions
    triple_syncs = []
    bit_offset = 0
    while bit_offset < total_bits - 48:
        word = (bits >> (total_bits - 16 - bit_offset)) & mask
        if word == sync_pattern:
            # Check for triple
            w2 = (bits >> (total_bits - 32 - bit_offset)) & mask
            w3 = (bits >> (total_bits - 48 - bit_offset)) & mask
            if w2 == sync_pattern and w3 == sync_pattern:
                triple_syncs.append(bit_offset)
                bit_offset += 48  # skip past the triple sync
                continue
        bit_offset += 1

    # Process sync marks: alternate between ID marks (0xFE) and data marks (0xFB)
    sectors = {}
    current_sector_id = None

    for sync_pos in triple_syncs:
        after_sync = sync_pos + 48  # bit position after 3x 0x4489

        # Decode the mark byte
        mark_byte = decode_mfm_word(bits, after_sync, total_bits)

        if mark_byte == 0xFE:
            # Sector ID Address Mark: cylinder, head, sector, size_code, CRC(2)
            header, _ = decode_mfm_bytes(bits, after_sync + 16, 4, total_bits)
            current_sector_id = header[2]  # sector number (1-based)

        elif mark_byte == 0xFB and current_sector_id is not None:
            # Sector Data Mark: sector_size bytes + CRC(2)
            data, _ = decode_mfm_bytes(bits, after_sync + 16, sector_size, total_bits)
            sectors[current_sector_id] = data
            current_sector_id = None

    return sectors


def main():
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <input.ipf> [output.st]")
        sys.exit(1)

    ipf_path = os.path.abspath(sys.argv[1])
    out_path = sys.argv[2] if len(sys.argv) >= 3 else os.path.splitext(ipf_path)[0] + ".st"

    lib_path = find_capsimg()
    if not lib_path:
        print("ERROR: capsimg library not found.")
        sys.exit(1)

    print(f"Loading capsimg: {lib_path}")
    caps = ctypes.CDLL(lib_path)

    assert caps.CAPSInit() == 0
    img_id = caps.CAPSAddImage()
    assert img_id >= 0
    assert caps.CAPSLockImage(img_id, ipf_path.encode()) == 0
    assert caps.CAPSLoadImage(img_id, ctypes.c_uint32(0)) == 0

    info = CapsImageInfo()
    assert caps.CAPSGetImageInfo(ctypes.byref(info), img_id) == 0

    platforms = {1: "Amiga", 2: "Atari ST", 3: "PC", 4: "Amstrad CPC"}
    num_cyls = info.maxcylinder - info.mincylinder + 1
    num_heads = info.maxhead - info.minhead + 1
    spt = 9
    sector_size = 512

    print(f"Platform: {platforms.get(info.platform[0], '?')}")
    print(f"Cylinders: {info.mincylinder}-{info.maxcylinder}, Heads: {info.minhead}-{info.maxhead}")
    print(f"Expected: {num_cyls * num_heads * spt * sector_size} bytes "
          f"({num_cyls}cyl x {num_heads}heads x {spt}spt x {sector_size}bps)")

    raw_disk = bytearray()
    total_decoded = 0
    total_empty = 0

    for cyl in range(info.mincylinder, info.maxcylinder + 1):
        for head in range(info.minhead, info.maxhead + 1):
            ti = CapsTrackInfo()
            flags = DI_LOCK_INDEX | DI_LOCK_ALIGN
            ret = caps.CAPSLockTrack(ctypes.byref(ti), img_id, cyl, head, ctypes.c_uint32(flags))

            if ret != 0 or ti.tracksize[0] == 0:
                raw_disk.extend(b'\x00' * spt * sector_size)
                total_empty += 1
                if ret == 0:
                    caps.CAPSUnlockTrack(img_id, cyl, head)
                continue

            mfm = bytes(ti.trackdata[0][0:ti.tracksize[0]])
            sectors = decode_mfm_track(mfm, sector_size)

            n_decoded = 0
            for sec_num in range(1, spt + 1):
                if sec_num in sectors:
                    raw_disk.extend(sectors[sec_num])
                    n_decoded += 1
                else:
                    raw_disk.extend(b'\x00' * sector_size)

            total_decoded += n_decoded
            if n_decoded == 0:
                total_empty += 1

            if cyl < 3 or (cyl == info.maxcylinder and head == 0):
                print(f"  Track {cyl:2d}/{head}: {n_decoded}/{spt} sectors decoded")

            caps.CAPSUnlockTrack(img_id, cyl, head)

    print(f"\nTotal: {total_decoded} sectors decoded, {total_empty} empty tracks")
    print(f"Disk image: {len(raw_disk)} bytes")

    with open(out_path, "wb") as f:
        f.write(raw_disk)
    print(f"Written: {out_path}")

    # Analyze boot sector
    boot = raw_disk[:512]
    bps = struct.unpack_from('<H', boot, 11)[0]
    spc = boot[13]
    res = struct.unpack_from('<H', boot, 14)[0]
    nfats = boot[16]
    rootents = struct.unpack_from('<H', boot, 17)[0]
    totsec = struct.unpack_from('<H', boot, 19)[0]
    spf = struct.unpack_from('<H', boot, 22)[0]
    spt_boot = struct.unpack_from('<H', boot, 24)[0]
    heads_boot = struct.unpack_from('<H', boot, 26)[0]

    print(f"\n--- Boot sector ---")
    print(f"  BPS={bps} SPC={spc} Reserved={res} FATs={nfats}")
    print(f"  Root entries={rootents} Total sectors={totsec}")
    print(f"  SPF={spf} SPT={spt_boot} Heads={heads_boot}")

    if bps > 0 and rootents > 0 and spf > 0 and nfats > 0:
        root_start = (res + nfats * spf) * bps
        data_start = root_start + rootents * 32

        print(f"\n--- Directory listing ---")
        for i in range(rootents):
            offset = root_start + i * 32
            if offset + 32 > len(raw_disk):
                break
            entry = raw_disk[offset:offset + 32]
            if entry[0] == 0x00:
                break
            if entry[0] == 0xE5 or entry[11] == 0x0F:
                continue

            name = entry[0:8].decode('ascii', errors='replace').rstrip()
            ext = entry[8:11].decode('ascii', errors='replace').rstrip()
            size = struct.unpack_from('<I', entry, 28)[0]
            cluster = struct.unpack_from('<H', entry, 26)[0]
            fname = f"{name}.{ext}" if ext else name
            print(f"  {fname:15s}  {size:8d} bytes  (cluster {cluster})")

    # Also dump the raw binary for exploration
    bin_path = os.path.splitext(out_path)[0] + "_memory.bin"

    # Cleanup
    caps.CAPSUnlockImage(img_id)
    caps.CAPSRemImage(img_id)
    caps.CAPSExit()


if __name__ == "__main__":
    main()
