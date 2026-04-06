package northern.captain.tools;

import northern.captain.gamecore.glx.NCore;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class KeyStore implements IKeyStore
{
	List<byte[]> storage = new ArrayList<byte[]>();
	Random rnd = new Random();
	int delta = 0;
	
	public KeyStore(String keys, int delta)
	{
		this(keys);
		this.delta = delta;
	}
	
	public KeyStore(String keys)
	{
		if(keys.length() == 0)
			return;
		
		byte[] debased = MyBase64Web.decode(keys);
		
		byte[] mkey = new byte[FSETCipher.CHUNK_SIZE];
		FSETCipher.putLongToByte(NCore.instance().getUUID().getMostSignificantBits(), mkey, 0);
		FSETCipher.putLongToByte(NCore.instance().getUUID().getLeastSignificantBits(), mkey, FSETCipher.CHUNK_SIZE/2);
		
		FSETCipher cipher = new FSETCipher(mkey);
		byte[] buf = cipher.decrypt(debased);
		String strbuf = new String(buf);
		
		try
		{
			JSONArray jar = new JSONArray(strbuf);
			for(int i=0;i<jar.length();i++)
			{
				UUID uuid = UUID.fromString(jar.getString(i));
				byte [] key = new byte[FSETCipher.CHUNK_SIZE];
				FSETCipher.putLongToByte(uuid.getMostSignificantBits(), key, 0);
				FSETCipher.putLongToByte(uuid.getLeastSignificantBits(), key, FSETCipher.CHUNK_SIZE/2);
				storage.add(key);
			}
		}
		catch (JSONException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.game.base.IKeyStore#getKey(int)
	 */
	@Override
	public byte[] getKey(int idx)
	{
		return storage.get(idx - delta);
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.game.base.IKeyStore#getKeyIdx()
	 */
	@Override
	public int getKeyIdx()
	{
		return rnd.nextInt(storage.size()) + delta;
	}
}
