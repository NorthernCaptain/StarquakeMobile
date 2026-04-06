package northern.captain.tools;

import java.io.InputStream;
import java.io.OutputStream;

public interface IPersistentConfig
{

	boolean contains(String arg0);

	boolean getBoolean(String key, boolean defValue);

	float getFloat(String key, float defValue);

	int getInt(String key, int defValue);

	long getLong(String key, long defValue);

	String getString(String key, String defValue);

	void setInt(String key, int Value);

	void setBoolean(String key, boolean Value);

	void setString(String key, String Value);

	void addOptionsChangeListener(IOptionsChangeListener listener);

	void removeOptionsChangeListener(IOptionsChangeListener listener);

	/**
	 * return File stream for writing persistent data into it
	 * 
	 * @param name
	 * @return
	 */
	OutputStream getFileForWriting(String name);

	/**
	 * Return file input stream for reading persistent data from it
	 * 
	 * @param name
	 * @return
	 */
	InputStream getFileForReading(String name);

	/**
	 * Commit all changes made by set* methods
	 */
	void commitChanges();

	void setAutoCommit(boolean val);

}
