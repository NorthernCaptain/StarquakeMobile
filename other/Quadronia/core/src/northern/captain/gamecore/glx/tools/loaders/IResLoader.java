package northern.captain.gamecore.glx.tools.loaders;

/**
 * Interface for resource loader
 * @author leo
 *
 */
public interface IResLoader
{
	/**
	 * Loads texture atlas with the given name from resources
	 * @param name
	 * @return
	 */
	<T> int loadAsync(String name, Class<T> clazz);

	int loadTextureAtlas(String name);
	
	<T> T getLoaded(int id);
	
	boolean isLoaded(int id);
	
	void unload(int id);
	
	void initialize();
	
	/**
	 * Loads one more queued resource
	 * @return true if all resources are loaded or false if we need more loading
	 */
	boolean update();
	
	/**
	 * Getter for data path prefix - usually 'data/'
	 * @return
	 */
	String getDataPathPrefix();

	/**
	 * Starts loading font generator
	 * @param name
	 * @return
	 */
	int loadFontGenerator(String name);
	
	void finishLoading();

	int loadXMLContent(String name);
	
	int loadLayout(String name);
	
	int loadSound(String name);

    int loadEffect(String name);
}
