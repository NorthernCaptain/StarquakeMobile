package northern.captain.gamecore.glx.tools.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;

/**
 * Asset loader for FreeTypeFontGenerators
 * @author leo
 *
 */
public class FreeTypeAssetLoader extends AsynchronousAssetLoader<FreeTypeFontGenerator, FreeTypeAssetLoader.Parameters>
{

	static public class Parameters extends AssetLoaderParameters<FreeTypeFontGenerator>
	{
		
	}
		
	public FreeTypeAssetLoader(FileHandleResolver resolver)
	{
		super(resolver);
	}

	@Override
	public FreeTypeFontGenerator loadSync(AssetManager assetManager, String fileName, FileHandle handle,
										FreeTypeAssetLoader.Parameters parameter)
	{
		return generator;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle handle,
			FreeTypeAssetLoader.Parameters parameter)
	{
		return null;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle handle,
			Parameters parameter)
	{
		generator = new FreeTypeFontGenerator(handle);
	}

	
	private FreeTypeFontGenerator generator;
}
