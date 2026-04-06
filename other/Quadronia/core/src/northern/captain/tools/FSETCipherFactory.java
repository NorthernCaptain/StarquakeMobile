/**
 * 
 */
package northern.captain.tools;

/**
 * @author leo
 *
 */
public class FSETCipherFactory implements ICipherFactory
{
	protected static FSETCipherFactory singleton;
	
	public static ICipherFactory instance()
	{
		if(singleton == null)
		{
			singleton = new FSETCipherFactory();
		}
		return singleton;
	}
	
	private FSETCipher[] ciphers = new FSETCipher[MAX_SCHEMAS];
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.game.base.ICipherFactory#createCipher(byte[])
	 */
	@Override
	public FSETCipher createCipher(byte[] key)
	{
		FSETCipher cipher = new FSETCipher(key);
		return cipher;
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.game.base.ICipherFactory#forSchema(int, byte[])
	 */
	@Override
	public synchronized ICipher forSchema(int schema, byte[] key)
	{
		if(schema < 0 || schema >= MAX_SCHEMAS)
			return null;
		if(ciphers[schema] == null)
		{
			ciphers[schema] = createCipher(key);
			ciphers[schema].setSchema(schema);
		}
		return ciphers[schema];
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.game.base.ICipherFactory#getChunkSchema(byte[])
	 */
	@Override
	public int getChunkSchema(byte[] chunk)
	{
		return FSETCipher.getChunkSchema(chunk);
	}
	
}
