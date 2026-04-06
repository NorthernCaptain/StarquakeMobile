package northern.captain.gamecore.glx.tools.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class XMLLayoutAssetLoader extends AsynchronousAssetLoader<XMLLayoutLoader, XMLLayoutAssetLoader.Parameters>
{
	static public class Parameters extends AssetLoaderParameters<XMLLayoutLoader>
	{
		
	}

	public XMLLayoutAssetLoader(FileHandleResolver resolver)
	{
		super(resolver);
	}
	
	private XMLLayoutLoader loader;
	
	@Override
	public void loadAsync(AssetManager manager, String fileName,  FileHandle handle,
			XMLLayoutAssetLoader.Parameters parameter)
	{
		loader = new XMLLayoutLoader();
		loader.loadXML(handle);		
	}

	@Override
	public XMLLayoutLoader loadSync(AssetManager manager, String fileName, FileHandle handle,
			XMLLayoutAssetLoader.Parameters parameter)
	{
		return loader;
	}

	@Override
	public Array getDependencies(String fileName, FileHandle handle,
			XMLLayoutAssetLoader.Parameters parameter)
	{
		return null;
	}

}
