package northern.captain.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public interface IPersistCfg 
{
	/**
	 * Method for saving persistent data into given config object
	 * @param cfg
	 */
	void saveData(IPersistentConfig cfg);
	
	/**
	 * Stores persistent data to file stream
	 * @param fout
	 */
	void saveData(FileOutputStream fout);
	/**
	 * Method for loading persistent data from config object
	 * @param cfg
	 */
	void loadData(IPersistentConfig cfg);
	/**
	 * Reads persistent data from file stream
	 * @param fin
	 */
	void loadData(FileInputStream fin);
}
