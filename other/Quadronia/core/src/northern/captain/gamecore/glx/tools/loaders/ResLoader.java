package northern.captain.gamecore.glx.tools.loaders;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import northern.captain.gamecore.glx.NContext;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;

/**
 * Class that loads all types of resources according to the current game configuration
 * @author leo
 *
 */
public class ResLoader implements IResLoader
{
    protected static final int DELTA_IDX = 11;
	protected AssetManager amanager;
	
	protected Array<String> resNames = new Array<String>(true, 32, String.class);
	
	public ResLoader()
	{
		
	}
	
	protected static ResLoader globalLoader = new ResLoader();
	
	static public IResLoader singleton()
	{
		return globalLoader;
	}

	@Override
	public void initialize()
	{
		FileHandleResolver resolver = new InternalFileHandleResolver();
		amanager = new AssetManager(resolver);
		amanager.setLoader(FreeTypeFontGenerator.class, new FreeTypeAssetLoader(resolver));
		amanager.setLoader(XMLContentLoader.class, new XMLContentAssetLoader(resolver));
		amanager.setLoader(XMLLayoutLoader.class, new XMLLayoutAssetLoader(resolver));
        amanager.setLoader(ParticleEffect.class, new ParticleEffectAssetLoader(resolver));
	}

	protected int getId(String name)
	{
		for(int i=0;i<resNames.size;i++)
		{
			Object nm = resNames.items[i];
			if(name.equals(nm))
				return i + DELTA_IDX;
		}
		return -1;
	}
	
	@Override
	public <T> int loadAsync(String name, Class<T> clazz)
	{
		String fullName = name;
		int id = getId(fullName);
		
		if(id<0)
		{
			id = resNames.size + DELTA_IDX;
			resNames.add(fullName);
		}
		
		amanager.load(fullName, clazz);
		return id;
	}

	@Override
	public <T> T getLoaded(int id)
	{
		String fullName = resNames.get(id - DELTA_IDX);
		return amanager.get(fullName);
	}

	@Override
	public boolean isLoaded(int id)
	{
		String fullName = resNames.get(id - DELTA_IDX);
		return amanager.isLoaded(fullName);
	}

	@Override
	public boolean update()
	{
		return amanager.update();
	}

	@Override
	public String getDataPathPrefix()
	{
		return "data/";
	}

	@Override
	public int loadTextureAtlas(String name)
	{
		StringBuilder buf = new StringBuilder(NContext.current.getGfxDir()); 
		buf.append(name);
		buf.append(".atlas");
		return loadAsync(buf.toString(), TextureAtlas.class);
	}

	@Override
	public int loadFontGenerator(String name)
	{
		StringBuilder buf = new StringBuilder(NContext.current.getFontDir()); 
		buf.append(name);
		buf.append(".ttf");
		return loadAsync(buf.toString(), FreeTypeFontGenerator.class);
	}
	
	@Override
	public int loadXMLContent(String name)
	{
		return loadAsync(name, XMLContentLoader.class);		
	}
	
	@Override
	public int loadLayout(String name)
	{
		return loadAsync(NContext.current.getLayoutDir(name), XMLLayoutLoader.class);		
	}

    @Override
    public int loadEffect(String name)
    {
        return loadAsync(NContext.current.getEffectDir(name), ParticleEffect.class);
    }

    @Override
	public void unload(int id)
	{
		String fullName = resNames.get(id - DELTA_IDX);
		amanager.unload(fullName);
	}

	@Override
	public void finishLoading()
	{
		amanager.finishLoading();
	}

	@Override
	public int loadSound(String name)
	{
		return loadAsync(NContext.current.getSoundDir(name), Sound.class);
	}
	
}
