package northern.captain.gamecore.glx.tools.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Array;
import northern.captain.gamecore.glx.effect.MyParticleEffect;

public class ParticleEffectAssetLoader extends AsynchronousAssetLoader<ParticleEffect, ParticleEffectAssetLoader.Parameters>
{
	static public class Parameters extends AssetLoaderParameters<ParticleEffect>
	{

	}

	private ParticleEffect particleEffect;

	public ParticleEffectAssetLoader(FileHandleResolver resolver)
	{
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle handle,
			ParticleEffectAssetLoader.Parameters parameter)
	{
		particleEffect = new MyParticleEffect();
		particleEffect.loadEmitters(handle);
	}

	@Override
	public ParticleEffect loadSync(AssetManager manager, String fileName, FileHandle handle,
			ParticleEffectAssetLoader.Parameters parameter)
	{
		return particleEffect;
	}

	@Override
	public Array getDependencies(String fileName, FileHandle handle,
			ParticleEffectAssetLoader.Parameters parameter)
	{
		return null;
	}

}
