package northern.captain.tools;

import java.util.UUID;

public class FSETCipher implements ICipher
{
	public static final int CHUNK_SIZE=16;
	protected static final int CHAR_SIZE=256;
	protected static final int CYCLES = 8;
	private static final int ROTCYCLES = 4;
	
	protected byte[][] encMatrix = new byte[CHUNK_SIZE][CHAR_SIZE];
	protected byte[][] decMatrix = new byte[CHUNK_SIZE][CHAR_SIZE];
	
	protected byte[][] Kts = new byte[CYCLES][CHUNK_SIZE];
	protected byte[][] Ktp = new byte[CYCLES][ROTCYCLES];
	
	private static byte[] shiftBuf2 = new byte[CHAR_SIZE];
	
	protected int schema;
	
	public FSETCipher(byte[] secretKey)
	{
		this(secretKey, 0);
	}
	
	public FSETCipher(byte[] secretKey, int schema)
	{
		this.schema = schema;
		createEncodingMatrix(secretKey);
		createDecodingMatrix();
	}
	
	private void createEncodingMatrix(byte[] secretKey)
	{
		for(int i=0;i<CHUNK_SIZE;i++)
		{
			for(int j=0;j<CHAR_SIZE;j++)
			{
				encMatrix[i][j] = (byte)j;
			}
			int first = (i+1) % CHUNK_SIZE;
			int shift = (secretKey[first] & 0xff) + (secretKey[i] & 0xff);
			rShiftBytes(encMatrix[i], 0, CHAR_SIZE, shift);
		}
		for(int i = 0; i< CYCLES; i++)
			System.arraycopy(encMatrix[i], 0, Kts[i], 0, CHUNK_SIZE);
		for(int i = 0; i< CYCLES; i++)
			System.arraycopy(encMatrix[i], 0, Ktp[i], 0, ROTCYCLES);

	}
	
	private void createDecodingMatrix()
	{
		for(int i=0;i<CHUNK_SIZE;i++)
		{
			for(int j=0;j<CHAR_SIZE;j++)
			{
				decMatrix[i][(0xff & encMatrix[i][j])] = (byte)j;
			}
		}		
	}
	
	public static void putLongToByte(long value, byte[] ar, int startIdx)
	{
		for (int i = 0; i < 8; ++i) 
		{
		  ar[i + startIdx] = (byte) (value >> ((8 - i - 1)<< 3));
		}		
	}
	
//	private void rShift(byte[] ar, int startIdx, int len, int shift)
//	{
//		if(len <= 0)
//			return;
//		
//		int bitShift = shift % 8;
//		shift /= 8;
//		shift %= len;
//		
//		if(shift == 0 && bitShift == 0)
//			return;
//
//		rShiftBytes(ar, startIdx, len, shift);
//
//		int rmask = (1 << bitShift) -1;
//		int lmask = 0xff ^ rmask;
//		
//		int revShift = 8 - bitShift;
//		
//		byte nval = (byte) ((ar[startIdx + len -1] & rmask) << revShift);
//		for(int i=0;i<len;i++)
//		{
//			byte val = ar[startIdx+i];
//			byte nextval = (byte) ((val & rmask) << revShift);
//			ar[startIdx + i] = (byte)(((val & lmask) >> bitShift) | nval);
//			nval = nextval;
//		}
//	}
	
	protected byte[] getShiftBuf()
	{
		return shiftBuf2;
	}
	
	private void rShiftBytes(byte[] ar, int startIdx, int len, int shift)
	{
		if(len <= 0)
			return;
		
		shift %= len;
		if(shift == 0)
			return;
		
		byte[] shiftBuf = getShiftBuf();
		
		System.arraycopy(ar, startIdx + len - shift, shiftBuf, 0, shift);
		System.arraycopy(ar, startIdx, ar, startIdx+shift, len - shift);
		System.arraycopy(shiftBuf, 0, ar, startIdx, shift);
	}
	
	private void lShiftBytes(byte[] ar, int startIdx, int len, int shift)
	{
		if(len <= 0)
			return;
		
		shift %= len;
		if(shift == 0)
			return;
		
		byte[] shiftBuf = getShiftBuf();

		System.arraycopy(ar, startIdx, shiftBuf, 0, shift);
		System.arraycopy(ar, startIdx + shift, ar, startIdx, len - shift);
		System.arraycopy(shiftBuf, 0, ar, startIdx + len - shift, shift);
	}
	
	private void xorChunk(byte[] chunk, byte[] xorVal)
	{
		for(int i = 0; i<chunk.length; i++)
			chunk[i] ^= xorVal[i];
	}
	
	public static final int CAN_DECRYPT = -2;
	
	public int canDecrypt(byte[] chunk)
	{
		int chunkSchema = getChunkSchema(chunk);
		if(chunkSchema != this.schema)
			return chunkSchema;
		return CAN_DECRYPT;
	}
	
	public int getSchema()
	{
		return schema;
	}
	
	public void setSchema(int schema)
	{
		this.schema = schema;
	}
	
	public static int getChunkSchema(byte[] chunk)
	{
		if(chunk[0] != 'F' || chunk[1] != 'S')
			return -1;
		
		return chunk[2] - '0';
	}
	
	public byte[] encrypt(byte[] from)
	{
		synchronized (this)
		{
			return encryptNoLock(from);
		}
	}
	
	public byte[] encryptNoLock(byte[] from)
	{
		int nChunks = ((from.length / CHUNK_SIZE) + 1);
		byte[] cipher = new byte[(nChunks+1)*CHUNK_SIZE];
		byte[] chunk = new byte[CHUNK_SIZE];
		
		int total = from.length;
		
		chunk[0] = 'F';
		chunk[1] = 'S';
		chunk[2] = (byte) ('0' + schema);
		chunk[3] = (byte)(total >>> 24);
		chunk[4] = (byte)(total >>> 16);
		chunk[5] = (byte)(total >>>  8);
		chunk[6] = (byte)(total);

		//First chunk is always with system info
		System.arraycopy(chunk, 0, cipher, 0, CHUNK_SIZE);
			for(int i=0;i<nChunks;i++)
			{
				System.arraycopy(from, i*CHUNK_SIZE, chunk, 0, total>CHUNK_SIZE ? CHUNK_SIZE : total);
				total -= CHUNK_SIZE;
				encryptChunk(chunk);
				System.arraycopy(chunk, 0, cipher, (i+1)*CHUNK_SIZE, CHUNK_SIZE);
			}
		return cipher;
	}
	
	public byte[] decrypt(byte[] cipher)
	{
		synchronized (this)
		{
			return decryptNoLock(cipher);
		}
	}
	
	public synchronized byte[] decryptNoLock(byte[] cipher)
	{
		int nChunks = cipher.length / CHUNK_SIZE -1;
		byte[] chunk = new byte[CHUNK_SIZE];
		
		System.arraycopy(cipher, 0, chunk, 0, CHUNK_SIZE);
		
		int total = 
			(chunk[3] << 24)
        	+ ((chunk[4] & 0xFF) << 16)
        	+ ((chunk[5] & 0xFF) << 8)
        	+ (chunk[6] & 0xFF);
		
		
		byte[] result = new byte[total];
		for(int i = 0; i<nChunks; i++)
		{
			System.arraycopy(cipher, (i+1)*CHUNK_SIZE, chunk, 0, CHUNK_SIZE);
			decryptChunk(chunk);
			System.arraycopy(chunk, 0, result, i*CHUNK_SIZE, total<CHUNK_SIZE ? total : CHUNK_SIZE);
			total -= CHUNK_SIZE;
		}
		
		return result;
	}
	
	private void encryptChunk(byte[] chunk)
	{
		for(int i = 0; i<CHUNK_SIZE;i++)
			chunk[i] = encMatrix[i][0xff & chunk[i]];
		for(int i=0; i<CYCLES; i++)
		{
			xorChunk(chunk, Kts[i]);
			rShiftBytes(chunk, 0, CHUNK_SIZE, 0xff & Ktp[i][0]);
			rShiftBytes(chunk, 0, CHUNK_SIZE/2, 0xff & Ktp[i][1]);
			lShiftBytes(chunk, CHUNK_SIZE/2, CHUNK_SIZE/2, 0xff & Ktp[i][2]);			
			rShiftBytes(chunk, 0, CHUNK_SIZE, 0xff & Ktp[i][3]);			
		}
	}
	
	private void decryptChunk(byte[] chunk)
	{
		for(int i=CYCLES-1; i>=0; i--)
		{
			lShiftBytes(chunk, 0, CHUNK_SIZE, 0xff & Ktp[i][3]);			
			rShiftBytes(chunk, CHUNK_SIZE/2, CHUNK_SIZE/2, 0xff & Ktp[i][2]);			
			lShiftBytes(chunk, 0, CHUNK_SIZE/2, 0xff & Ktp[i][1]);
			lShiftBytes(chunk, 0, CHUNK_SIZE, 0xff & Ktp[i][0]);
			xorChunk(chunk, Kts[i]);
		}
		for(int i = 0; i<CHUNK_SIZE;i++)
			chunk[i] = decMatrix[i][0xff & chunk[i]];		
	}
	
	public static byte[] genKey()
	{
		UUID uuid = UUID.randomUUID();
		byte[] key = new byte[16];
		FSETCipher.putLongToByte(uuid.getMostSignificantBits(), key, 0);
		FSETCipher.putLongToByte(uuid.getLeastSignificantBits(), key, 8);
		return key;
	}
}
