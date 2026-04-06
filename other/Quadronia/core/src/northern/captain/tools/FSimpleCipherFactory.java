package northern.captain.tools;

public class FSimpleCipherFactory extends FSETCipherFactory 
{
	private static final FSimpleCipherFactory single = new FSimpleCipherFactory();
	
	public static FSimpleCipherFactory getSingleton()
	{
		return single;
	}
	
	public static void initStatic()
	{
	}
	
	public FSimpleCipherFactory() 
	{
	}

	@Override
	public FSETCipher createCipher(byte[] key) 
	{
		return new FSimpleCipher(key);
	}

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.game.base.ICipherFactory#getChunkSchema(byte[])
	 */
	@Override
	public int getChunkSchema(byte[] chunk)
	{
		return FSimpleCipher.getChunkSchema(chunk);
	}	
}
