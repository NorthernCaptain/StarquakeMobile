package northern.captain.gamecore.glx.tools.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class XMLContentAssetLoader extends AsynchronousAssetLoader<XMLContentLoader, XMLContentAssetLoader.Parameters>
{
	static public class Parameters extends AssetLoaderParameters<XMLContentLoader>
	{
		
	}

	private XMLContentLoader loader;
	
	public XMLContentAssetLoader(FileHandleResolver resolver)
	{
		super(resolver);
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle handle,
			XMLContentAssetLoader.Parameters parameter)
	{
		loader = new XMLContentLoader();
		loader.loadXML(handle);
	}

	@Override
	public XMLContentLoader loadSync(AssetManager manager, String fileName, FileHandle handle,
			XMLContentAssetLoader.Parameters parameter)
	{
		return loader;
	}

	@Override
	public Array getDependencies(String fileName, FileHandle handle,
			XMLContentAssetLoader.Parameters parameter)
	{
		return null;
	}

}
