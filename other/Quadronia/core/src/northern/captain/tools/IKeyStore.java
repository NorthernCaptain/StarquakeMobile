package northern.captain.tools;

public interface IKeyStore
{
	
	byte[] getKey(int idx);
	
	int getKeyIdx();
	
}
