package northern.captain.tools;

public class FSimpleCipher extends FSETCipher
{
	public FSimpleCipher(byte[] secretKey)
	{
		this(secretKey, 0);
	}
	
	public FSimpleCipher(byte[] secretKey, int schema)
	{
		super(secretKey, schema);
	}
	
	public int canDecrypt(byte[] chunk)
	{
		int chunkSchema = FSimpleCipher.getChunkSchema(chunk);
		if(chunkSchema != this.schema)
			return chunkSchema;
		return CAN_DECRYPT;
	}
	
	public static int getChunkSchema(byte[] chunk)
	{
		if(chunk[0] != 'S' || chunk[1] != 'S')
			return -1;
		
		return chunk[2] - '0';
	}
	
	public byte[] encrypt(byte[] from)
	{
		return encryptNoLock(from);
	}
	
	private static final int sysdelta = 8;
	public byte[] encryptNoLock(byte[] from)
	{
		byte[] cipher = new byte[from.length + sysdelta];
		
		int ischema = schema % CYCLES;
		int total = from.length;
		
		cipher[0] = 'S';
		cipher[1] = 'S';
		cipher[2] = (byte) ('0' + schema);
		cipher[3] = (byte)(total >>> 24);
		cipher[4] = (byte)(total >>> 16);
		cipher[5] = (byte)(total >>>  8);
		cipher[6] = (byte)(total);

		int ich = 0;
		for(int i=0;i<from.length;i++)
		{
			ich = i % CHUNK_SIZE;
			cipher[sysdelta + i] = encMatrix[ich][(0xff & from[i])];
			cipher[sysdelta + i] ^= Kts[ischema][ich];
		}
		return cipher;
	}
	
	public byte[] decrypt(byte[] cipher)
	{
		return decryptNoLock(cipher);
	}
	
	public byte[] decryptNoLock(byte[] cipher)
	{
		int total = 
			(cipher[3] << 24)
        	+ ((cipher[4] & 0xFF) << 16)
        	+ ((cipher[5] & 0xFF) << 8)
        	+ (cipher[6] & 0xFF);
		
		
		int ischema = schema % CYCLES;
		int ich = 0;

		byte[] result = new byte[total];
		for(int i = 0; i<total; i++)
		{
			ich = i % CHUNK_SIZE;			
			result[i] = cipher[i+sysdelta];
			result[i] ^= Kts[ischema][ich];
			result[i] = decMatrix[ich][(0xff & result[i])];
		}
		
		return result;
	}
}
