#!/usr/bin/env python3
"""
Extract Atari ST Starquake music and sound effects from the RAM dump.

This replays the resident PSG/YM2149 driver state and script tables found in
the Hatari 1 MB RAM snapshot, then renders the resulting register stream to WAV.

Usage:
    python3 tools/extract_st_audio.py research/assets/atarist/st_ram_full.bin
"""

from __future__ import annotations

import json
import math
import os
import struct
import sys
import wave
from dataclasses import dataclass
from pathlib import Path


RAM_SIZE = 0x100000

GLOBAL_BASE = 0x3B48A
MUSIC_STATE_BASE = 0x3B9B6
MUSIC_STATE_SIZE = 0x1A
MUSIC_DATA_BASE = 0x3BA04
MUSIC_TABLE = 0x3BB13
MUSIC_COUNT = 5
PATTERN_TABLE = 0x3BB5C
NOTE_TABLE = 0x3B808
SFX_TABLE = 0x3C3BC
SFX_RECORD_SIZE = 0x12
SFX_SCAN_LIMIT = 32

TICK_HZ = 50
SAMPLE_RATE = 44100
PSG_CLOCK = 2_000_000


AY_LEVELS = [
    0.0,
    0.004654,
    0.007721,
    0.010956,
    0.016998,
    0.025253,
    0.037583,
    0.055838,
    0.082654,
    0.121929,
    0.179079,
    0.258289,
    0.372838,
    0.535887,
    0.762619,
    1.0,
]


def u8(v: int) -> int:
    return v & 0xFF


def s8(v: int) -> int:
    v &= 0xFF
    return v - 0x100 if v & 0x80 else v


def u16(v: int) -> int:
    return v & 0xFFFF


def s16(v: int) -> int:
    v &= 0xFFFF
    return v - 0x10000 if v & 0x8000 else v


def add8(a: int, b: int) -> tuple[int, bool]:
    total = (a & 0xFF) + (b & 0xFF)
    return total & 0xFF, total > 0xFF


def sub8(a: int, b: int) -> tuple[int, bool]:
    a &= 0xFF
    b &= 0xFF
    diff = a - b
    return diff & 0xFF, diff < 0


@dataclass
class SongDef:
    tempo: int
    sequence_bytes: list[int]
    offsets: list[int]


@dataclass
class SfxDef:
    words: list[int]


class PSGRenderer:
    def __init__(self, sample_rate: int = SAMPLE_RATE, clock: int = PSG_CLOCK):
        self.sample_rate = sample_rate
        self.clock = clock
        self.tone_phase = [0.0, 0.0, 0.0]
        self.noise_phase = 0.0
        self.noise_lfsr = 0x1FFFF
        self.noise_bit = 1
        self.env_phase = 0.0
        self.env_step = 15
        self.env_attack = 0
        self.env_hold = False
        self.env_alternate = False
        self.env_continue = False
        self.env_holding = False
        self.env_cycle = 0

    def trigger_envelope(self, shape: int) -> None:
        self.env_continue = bool(shape & 0x08)
        self.env_attack = 0x0F if (shape & 0x04) else 0x00
        self.env_alternate = bool(shape & 0x02)
        self.env_hold = bool(shape & 0x01)
        self.env_step = 15
        self.env_holding = False
        self.env_cycle = 0
        self.env_phase = 0.0

    def envelope_level(self) -> float:
        if self.env_attack:
            idx = self.env_step ^ 0x0F
        else:
            idx = self.env_step
        return AY_LEVELS[idx & 0x0F]

    def _advance_envelope(self) -> None:
        if self.env_holding:
            return
        if self.env_step > 0:
            self.env_step -= 1
            return
        if not self.env_continue:
            self.env_holding = True
            self.env_step = 0
            return
        self.env_cycle += 1
        if self.env_hold:
            if self.env_alternate:
                self.env_attack ^= 0x0F
            self.env_holding = True
            self.env_step = 0
            return
        if self.env_alternate and (self.env_cycle & 1):
            self.env_attack ^= 0x0F
        self.env_step = 15

    def _advance_noise(self) -> None:
        feedback = ((self.noise_lfsr ^ (self.noise_lfsr >> 3)) & 1)
        self.noise_lfsr = (self.noise_lfsr >> 1) | (feedback << 16)
        self.noise_bit = self.noise_lfsr & 1

    def render_tick(self, regs: list[int]) -> list[float]:
        frame_samples = self.sample_rate // TICK_HZ

        tone_periods = []
        tone_freqs = []
        for idx in range(3):
            lo = regs[idx * 2]
            hi = regs[idx * 2 + 1] & 0x0F
            period = ((hi << 8) | lo) or 1
            tone_periods.append(period)
            tone_freqs.append(self.clock / (16.0 * period))

        noise_period = regs[6] & 0x1F
        if noise_period == 0:
            noise_period = 1
        noise_freq = self.clock / (16.0 * noise_period)

        env_period = (((regs[12] & 0xFF) << 8) | regs[11]) or 1
        env_freq = self.clock / (16.0 * env_period)
        if regs[13]:
            self.trigger_envelope(regs[13] & 0x0F)

        out = []
        for _ in range(frame_samples):
            self.noise_phase += noise_freq / self.sample_rate
            while self.noise_phase >= 1.0:
                self.noise_phase -= 1.0
                self._advance_noise()

            self.env_phase += env_freq / self.sample_rate
            while self.env_phase >= 1.0:
                self.env_phase -= 1.0
                self._advance_envelope()

            mix = 0.0
            mixer = regs[7]
            env_level = self.envelope_level()

            for ch in range(3):
                self.tone_phase[ch] += tone_freqs[ch] / self.sample_rate
                if self.tone_phase[ch] >= 1.0:
                    self.tone_phase[ch] -= math.floor(self.tone_phase[ch])

                tone_on = bool(mixer & (1 << ch))
                noise_on = bool(mixer & (1 << (ch + 3)))
                tone_bit = 1 if self.tone_phase[ch] < 0.5 else 0
                noise_bit = self.noise_bit
                gate = (tone_on or tone_bit) and (noise_on or noise_bit)

                volreg = regs[8 + ch]
                if volreg & 0x10:
                    level = env_level
                else:
                    level = AY_LEVELS[volreg & 0x0F]

                if gate:
                    mix += level

            out.append(max(-1.0, min(1.0, mix / 3.0)))

        return out


class StarquakeAudioDriver:
    def __init__(self, ram: bytes):
        if len(ram) != RAM_SIZE:
            raise ValueError(f"Expected 1 MB RAM dump, got {len(ram)} bytes")
        self.ram = bytearray(ram)

    def clone(self) -> "StarquakeAudioDriver":
        return StarquakeAudioDriver(bytes(self.ram))

    def rb(self, addr: int) -> int:
        return self.ram[addr]

    def wb(self, addr: int, val: int) -> None:
        self.ram[addr] = val & 0xFF

    def rw(self, addr: int) -> int:
        return struct.unpack_from(">H", self.ram, addr)[0]

    def sw(self, addr: int) -> int:
        return struct.unpack_from(">h", self.ram, addr)[0]

    def ww(self, addr: int, val: int) -> None:
        struct.pack_into(">H", self.ram, addr, val & 0xFFFF)

    def rl(self, addr: int) -> int:
        return struct.unpack_from(">I", self.ram, addr)[0]

    def wl(self, addr: int, val: int) -> None:
        struct.pack_into(">I", self.ram, addr, val & 0xFFFFFFFF)

    def channel_base(self, idx: int) -> int:
        return MUSIC_STATE_BASE + idx * MUSIC_STATE_SIZE

    def read_song_defs(self) -> list[SongDef]:
        songs = []
        for i in range(MUSIC_COUNT):
            off = MUSIC_TABLE + i * 14
            tempo = self.rb(off)
            seq = [
                self.rw(off + 1),
                self.rw(off + 5),
                self.rw(off + 9),
            ]
            sequence_bytes = [
                (seq[0] >> 8) & 0xFF,
                seq[0] & 0xFF,
                (seq[1] >> 8) & 0xFF,
                seq[1] & 0xFF,
                (seq[2] >> 8) & 0xFF,
                seq[2] & 0xFF,
            ]
            offsets = [
                self.rw(off + 3),
                self.rw(off + 7),
                self.rw(off + 11),
            ]
            songs.append(SongDef(tempo=tempo, sequence_bytes=sequence_bytes, offsets=offsets))
        return songs

    def read_sfx_defs(self) -> list[SfxDef]:
        defs = []
        for i in range(self.sfx_count()):
            off = SFX_TABLE + i * SFX_RECORD_SIZE
            defs.append(SfxDef(words=[self.rw(off + j * 2) for j in range(9)]))
        return defs

    def sfx_count(self) -> int:
        last_nonzero = -1
        for i in range(SFX_SCAN_LIMIT):
            off = SFX_TABLE + i * SFX_RECORD_SIZE
            words = [self.rw(off + j * 2) for j in range(9)]
            if any(words):
                last_nonzero = i
        return last_nonzero + 1

    def init_music(self, index: int) -> None:
        if not (0 <= index < MUSIC_COUNT):
            raise ValueError(index)
        base = MUSIC_TABLE + index * 14

        clear_ptr = MUSIC_DATA_BASE
        for _ in range(0x27):
            clear_ptr -= 2
            self.ww(clear_ptr, 0)

        self.wb(GLOBAL_BASE + 0x1E, 0)
        self.wb(GLOBAL_BASE + 0x22, 0)
        self.wb(GLOBAL_BASE + 0x24, 0xFF)

        self.wb(GLOBAL_BASE + 0x23, self.rb(base))
        ptr = base + 1

        for ch in range(3):
            cbase = self.channel_base(ch)
            self.ww(cbase + 0x06, self.rw(ptr))
            ptr += 2
            script_off = self.rw(ptr)
            ptr += 2
            self.wb(cbase + 0x10, 1)
            self.ww(cbase + 0x04, script_off)
            self.ww(cbase + 0x02, self.rw(GLOBAL_BASE + script_off))

        self.wb(GLOBAL_BASE + 0x1E, 0xFF)

    def init_sfx(self, index: int) -> None:
        if not (0 <= index < self.sfx_count()):
            raise ValueError(index)
        rec = SFX_TABLE + index * SFX_RECORD_SIZE
        g = GLOBAL_BASE
        self.wb(g + 0x1F, 0)
        for i in range(7):
            self.ww(g + 0x0E + i * 2, self.rw(rec + i * 2))
        self.ww(g + 0x0A, self.rw(rec + 14))
        self.ww(g + 0x0C, self.rw(rec + 16))
        self.ww(g + 0x1C, self.rw(g + 0x18))
        self.wb(g + 0x1F, 0xFF)

    def _cmd_sequence_jump(self, cbase: int) -> int:
        pos = self.rb(cbase + 0x01)
        table_off = self.rw(cbase + 0x04)
        pos = u8(pos + 2)
        if pos == self.rb(cbase + 0x06):
            pos = self.rb(cbase + 0x07)
        self.wb(cbase + 0x01, pos)
        return GLOBAL_BASE + self.rw(GLOBAL_BASE + table_off + pos)

    def _advance_channel(self, cbase: int) -> None:
        counter = u8(self.rb(cbase + 0x10) - 1)
        self.wb(cbase + 0x10, counter)
        if counter != 0:
            flags = self.rb(cbase)
            if flags & (1 << 6):
                note = self.rb(cbase + 0x12)
                if flags & (1 << 7):
                    note = u8(note - 1)
                else:
                    note = u8(note + 1)
                self.wb(cbase + 0x12, note)
            return

        self.wb(cbase, self.rb(cbase) & 0x30)
        ptr = GLOBAL_BASE + self.rw(cbase + 0x02)

        while True:
            cmd = self.rb(ptr)
            ptr += 1

            if cmd >= 0xB0:
                d0, carry = add8(cmd, 0x20)
                if carry:
                    self.wb(cbase + 0x11, u8(d0 + 1))
                    continue
                d0, carry = add8(d0, 0x20)
                if carry:
                    self.wb(cbase + 0x13, u8(d0 + 1))
                    continue
                d0 = u8(d0 + 0x10)
                self.wb(cbase + 0x0B, d0)
                continue

            if cmd < 0x80:
                self.ww(cbase + 0x0E, 0)
                if not (self.rb(cbase + 0x12) & 0x80):
                    self.ww(cbase + 0x08, 0)
                    self.wb(cbase + 0x18, 0)
                self.wb(cbase + 0x12, cmd)
                self.wb(cbase + 0x0A, 0)
                if cmd >= 0x54:
                    self.wb(cbase + 0x0A, 0x02)
                    self.wb(GLOBAL_BASE + 0x25, cmd - 0x54)
                self.wb(cbase + 0x10, self.rb(cbase + 0x11))
                self.ww(cbase + 0x02, ptr - GLOBAL_BASE)
                return

            cmd_idx = cmd - 0x80
            if cmd_idx == 0:
                self.wb(cbase + 0x09, 0xF0)
                self.wb(cbase + 0x10, self.rb(cbase + 0x11))
                self.ww(cbase + 0x02, ptr - GLOBAL_BASE)
                return
            if cmd_idx == 1:
                self.wb(cbase, 0)
                continue
            if cmd_idx == 2:
                self.wb(cbase + 0x0C, self.rb(ptr))
                self.wb(cbase + 0x0D, self.rb(ptr + 1))
                self.wb(cbase + 0x19, self.rb(ptr + 2))
                self.wb(cbase, self.rb(cbase) | (1 << 3))
                ptr += 3
                continue
            if cmd_idx == 3:
                self.wb(cbase, self.rb(cbase) | (1 << 7) | (1 << 6))
                continue
            if cmd_idx == 4:
                self.wb(cbase, self.rb(cbase) | (1 << 6))
                continue
            if cmd_idx == 5:
                ptr = self._cmd_sequence_jump(cbase)
                continue
            if cmd_idx == 6:
                step = self.rb(ptr)
                span = self.rb(ptr + 1)
                self.wb(cbase + 0x15, step)
                self.wb(cbase + 0x16, span)
                self.wb(cbase + 0x14, u8(span + span))
                self.wb(cbase, self.rb(cbase) | (1 << 4))
                ptr += 2
                continue
            if cmd_idx == 7:
                self.wb(cbase, self.rb(cbase) | (1 << 1))
                continue
            if cmd_idx == 8:
                self.wb(GLOBAL_BASE + 0x17, 0)
                self.wb(GLOBAL_BASE + 0x07, 0)
                self.ww(GLOBAL_BASE + 0x08, 0)
                return
            if cmd_idx == 9:
                self.wb(cbase + 0x17, self.rb(ptr))
                ptr += 1
                continue
            if cmd_idx == 10:
                self.wb(cbase, self.rb(cbase) | (1 << 2) | (1 << 1))
                continue
            if cmd_idx == 11:
                self.wb(GLOBAL_BASE + 0x26, self.rb(ptr))
                self.wb(cbase, self.rb(cbase) | (1 << 2) | (1 << 1))
                ptr += 1
                continue
            if cmd_idx == 12:
                self.wb(cbase + 0x12, 0xFF)
                continue

            self.ww(cbase + 0x02, ptr - GLOBAL_BASE)
            return

    def _calc_channel(self, cbase: int, out_off: int) -> None:
        state9 = self.rb(cbase + 0x09)
        if state9 < 0xF0:
            state9, borrow = sub8(state9, 0x10)
            self.wb(cbase + 0x09, state9)
            if borrow:
                d0 = self.rb(cbase + 0x13)
                d0 = self.rb(PATTERN_TABLE + 0x0E + d0)
                d0 = u8(d0 + self.rb(cbase + 0x08))
                self.wb(cbase + 0x08, u8(self.rb(cbase + 0x08) + 1))
                self.wb(cbase + 0x09, self.rb(PATTERN_TABLE + 0x17 + d0))

        d0 = self.rb(cbase + 0x09) | 0xF0
        d0, carry = add8(d0, self.rb(GLOBAL_BASE + 0x21))
        if not carry:
            d0 = 0xFF
        d0 = u8(d0 + 1)
        self.wb(GLOBAL_BASE + out_off, d0)

        idx = self.rb(cbase + 0x0B)
        idx = self.rb(PATTERN_TABLE - 4 + idx)
        idx = u8(idx + self.rb(cbase + 0x18))
        note_delta = self.rb(PATTERN_TABLE + idx)
        if note_delta & 0x80:
            note_delta = u8(note_delta + 1)
            if note_delta != 0:
                note_delta = u8(note_delta - 0x81)
                self.wb(cbase + 0x18, 0xFF)
        self.wb(cbase + 0x18, u8(self.rb(cbase + 0x18) + 1))

        note = u8(note_delta + self.rb(cbase + 0x12))
        note = u8(note + self.rb(cbase + 0x17))
        note_index = min(max(note, 0), 31)
        period = self.rw(NOTE_TABLE + note_index * 2)

        if self.rb(cbase) & (1 << 4):
            limit = self.rb(cbase + 0x14)
            vib = self.rb(cbase + 0x16)
            if self.rb(cbase) & (1 << 5):
                vib = u8(vib + self.rb(cbase + 0x15))
            else:
                vib = u8(vib - self.rb(cbase + 0x15))
            if vib == limit:
                self.wb(cbase, self.rb(cbase) ^ (1 << 5))
            self.wb(cbase + 0x16, vib)
            half = (limit >> 1) & 0xFF
            delta, borrow = sub8(vib, half)
            if borrow:
                period = u16(period - 0x100)
            period = u16(period + delta)

        self.wb(cbase, self.rb(cbase) ^ 0x01)
        if self.rb(cbase) & (1 << 3):
            if self.rb(cbase + 0x19) == 0:
                d2 = self.rw(cbase + 0x0C)
                self.ww(cbase + 0x0E, self.rw(cbase + 0x0E) + d2)
                period = u16(period + self.rw(cbase + 0x0E))
            else:
                self.wb(cbase + 0x19, u8(self.rb(cbase + 0x19) - 1))

        vol = 0
        if self.rb(cbase + 0x0A) & (1 << 1):
            vol = 3
        if self.rb(cbase) & (1 << 1):
            if self.rb(cbase) & 0x01:
                self.wb(GLOBAL_BASE + 0x06, self.rb(GLOBAL_BASE + 0x26))
                vol |= 1
        if self.rb(cbase) & (1 << 2):
            self.wb(cbase, self.rb(cbase) & ~(1 << 1))
        self.wb(cbase + 0x0A, vol)

        self.ww(GLOBAL_BASE + out_off - 7, period)

    def _update_sfx(self) -> None:
        g = GLOBAL_BASE
        if self.rb(g + 0x1F) == 0:
            return

        self.wb(g + 0x0D, u8(self.rb(g + 0x0D) - 1))
        if self.rb(g + 0x0D) == 0:
            self.wb(g + 0x09, 0)
            self.wb(g + 0x1F, 0)
            return

        if self.rb(g + 0x19):
            self.wb(g + 0x1D, u8(self.rb(g + 0x1D) - 1))
            if self.rb(g + 0x1D) == 0:
                self.wb(g + 0x1D, self.rb(g + 0x19))
                d0 = self.rl(g + 0x14)
                d1 = self.rb(g + 0x1A)
                carry = d1 & 1
                d1 = u8((d1 >> 1) | ((d1 & 1) << 7))
                if carry:
                    d0 = ((d0 & 0xFFFF) << 16) | ((d0 >> 16) & 0xFFFF)
                self.wb(g + 0x1A, d1)
                self.ww(g + 0x10, self.rw(g + 0x10) + (d0 & 0xFFFF))

        self.ww(g + 0x10, self.rw(g + 0x10) + self.rw(g + 0x0E))

        if self.rb(g + 0x18):
            self.wb(g + 0x1C, u8(self.rb(g + 0x1C) - 1))
            if self.rb(g + 0x1C) == 0:
                self.wb(g + 0x1C, self.rb(g + 0x18))
                self.ww(g + 0x10, self.rw(g + 0x12))

        self.wb(g + 0x09, 0x10)
        self.ww(g + 0x04, self.rw(g + 0x10))

        cbase = self.channel_base(2)
        self.wb(cbase + 0x0A, self.rb(cbase + 0x0A) & ~0x01)

        d0 = self.rb(g + 0x1B)
        carry = d0 & 1
        d0 = u8((d0 >> 1) | ((d0 & 1) << 7))
        if carry:
            self.wb(cbase + 0x0A, u8(self.rb(cbase + 0x0A) + 1))
            self.wb(g + 0x06, self.rb(g + 0x11))
        self.wb(g + 0x1B, d0)

    def _commit_shadow_regs(self) -> list[int]:
        mixer = 0xF8
        if self.rb(self.channel_base(0) + 0x0A) & 0x01:
            mixer ^= 0x09
        if self.rb(self.channel_base(1) + 0x0A) & 0x01:
            mixer ^= 0x12
        if self.rb(self.channel_base(2) + 0x0A) & 0x01:
            mixer ^= 0x24

        regs = [0] * 16
        regs[1] = self.rb(GLOBAL_BASE + 0x00)
        regs[0] = self.rb(GLOBAL_BASE + 0x01)
        regs[3] = self.rb(GLOBAL_BASE + 0x02)
        regs[2] = self.rb(GLOBAL_BASE + 0x03)
        regs[5] = self.rb(GLOBAL_BASE + 0x04)
        regs[4] = self.rb(GLOBAL_BASE + 0x05)
        regs[6] = self.rb(GLOBAL_BASE + 0x06)
        regs[7] = mixer
        regs[8] = self.rb(GLOBAL_BASE + 0x07)
        regs[9] = self.rb(GLOBAL_BASE + 0x08)
        regs[10] = self.rb(GLOBAL_BASE + 0x09)
        regs[12] = self.rb(GLOBAL_BASE + 0x0A)
        regs[11] = self.rb(GLOBAL_BASE + 0x0B)
        regs[13] = self.rb(GLOBAL_BASE + 0x0C)
        if regs[13]:
            self.wb(GLOBAL_BASE + 0x0C, 0)
        return regs

    def tick(self) -> list[int]:
        if self.rb(GLOBAL_BASE + 0x1E):
            _, carry = add8(self.rb(GLOBAL_BASE + 0x22), self.rb(GLOBAL_BASE + 0x20))
            self.wb(GLOBAL_BASE + 0x22, u8(self.rb(GLOBAL_BASE + 0x22) + self.rb(GLOBAL_BASE + 0x20)))
            if not carry:
                _, carry2 = add8(self.rb(GLOBAL_BASE + 0x24), self.rb(GLOBAL_BASE + 0x23))
                self.wb(GLOBAL_BASE + 0x24, u8(self.rb(GLOBAL_BASE + 0x24) + self.rb(GLOBAL_BASE + 0x23)))
                if carry2:
                    for ch in range(3):
                        self._advance_channel(self.channel_base(ch))

            self._calc_channel(self.channel_base(0), 0x07)
            self._calc_channel(self.channel_base(1), 0x08)
            self._calc_channel(self.channel_base(2), 0x09)

        self._update_sfx()
        return self._commit_shadow_regs()


def render_driver_audio(driver: StarquakeAudioDriver, seconds: float) -> tuple[list[float], list[list[int]]]:
    renderer = PSGRenderer()
    frames = int(seconds * TICK_HZ)
    samples: list[float] = []
    regs_log: list[list[int]] = []
    for _ in range(frames):
        regs = driver.tick()
        regs_log.append(regs[:14])
        samples.extend(renderer.render_tick(regs))
    return samples, regs_log


def trim_audio(samples: list[float], floor: float = 0.001, tail_seconds: float = 0.25) -> list[float]:
    if not samples:
        return samples
    tail = int(SAMPLE_RATE * tail_seconds)
    end = len(samples)
    while end > tail and max(abs(v) for v in samples[end - tail:end]) < floor:
        end -= tail
    return samples[:max(end, tail)]


def write_wav(path: Path, samples: list[float]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for s in samples:
            v = max(-32767, min(32767, int(s * 20000)))
            frames += struct.pack("<h", v)
        wav.writeframes(bytes(frames))


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: python3 tools/extract_st_audio.py research/assets/atarist/st_ram_full.bin")
        return 1

    ram_path = Path(sys.argv[1])
    ram = ram_path.read_bytes()
    driver = StarquakeAudioDriver(ram)

    outdir = Path("research/assets/atarist/extracted/audio")
    music_dir = outdir / "music"
    sfx_dir = outdir / "sfx"
    outdir.mkdir(parents=True, exist_ok=True)

    metadata = {
        "source_ram": str(ram_path),
        "tick_hz": TICK_HZ,
        "sample_rate": SAMPLE_RATE,
        "music": [],
        "sfx": [],
    }

    for idx in range(MUSIC_COUNT):
        d = driver.clone()
        d.init_music(idx)
        samples, regs = render_driver_audio(d, seconds=45.0)
        samples = trim_audio(samples, floor=0.0005, tail_seconds=1.0)
        path = music_dir / f"music_{idx:02d}.wav"
        write_wav(path, samples)
        metadata["music"].append({
            "index": idx,
            "wav": str(path),
            "samples": len(samples),
            "seconds": round(len(samples) / SAMPLE_RATE, 3),
            "first_regs": regs[:8],
        })
        print(f"music {idx:02d}: {path}")

    sfx_defs = driver.read_sfx_defs()
    for idx in range(len(sfx_defs)):
        d = driver.clone()
        d.init_sfx(idx)
        samples, regs = render_driver_audio(d, seconds=4.0)
        samples = trim_audio(samples, floor=0.0005, tail_seconds=0.3)
        path = sfx_dir / f"sfx_{idx:02d}.wav"
        write_wav(path, samples)
        metadata["sfx"].append({
            "index": idx,
            "wav": str(path),
            "samples": len(samples),
            "seconds": round(len(samples) / SAMPLE_RATE, 3),
            "first_regs": regs[:8],
            "words": sfx_defs[idx].words,
        })
        print(f"sfx   {idx:02d}: {path}")

    with open(outdir / "audio_catalog.json", "w") as f:
        json.dump(metadata, f, indent=2)

    print(f"catalog: {outdir / 'audio_catalog.json'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
