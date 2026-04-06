package northern.captain.tools;

public interface ICipher
{
	/**
	 * Encrypt given byte sequence. Can return more bytes than original buffer
	 * @param from
	 * @return encrypted byte sequence.
	 */
	byte[] encrypt(byte[] from);
	
	/**
	 * Decrypt encrypted buffer
	 * @param enc
	 * @return decrypted buffer
	 */
	byte[] decrypt(byte[] enc);
}
