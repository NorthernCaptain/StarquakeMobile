package northern.captain.gamecore.glx.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.loaders.IResLoader;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLContentLoader;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SharedRes
{
	public static SharedRes instance = new SharedRes();
	
	protected XMLContentLoader i18n;
	protected XMLContentLoader common;
	protected TextureAtlas commonAtlas;
	
	
	protected class LoadIds
	{
		public int i18n;
		public int i18nlocal = -1;
		public int mainFont;
        public int sansFont;
		public int common;
		public int commonAtlas;
	}
	
	protected LoadIds load = new LoadIds();

	protected BitmapFont defaultFont;
	
	protected Map<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	
	public void startLoadingPart1()
	{
        IResLoader loader = ResLoader.singleton();
        load.commonAtlas = loader.loadTextureAtlas("common-atlas/atlas");
	}

	public void endLoadingPart1()
	{
        IResLoader loader = ResLoader.singleton();

        commonAtlas = loader.getLoaded(load.commonAtlas);
	}

	public void startLoadingPart2()
	{
		IResLoader loader = ResLoader.singleton();

        NContext.current.addRefresh();
        String langPath = NContext.current.getLangFilePath();
        if(!NContext.current.getDefaultLangFilePath().equals(langPath))
        	load.i18nlocal = loader.loadXMLContent(langPath);
        load.i18n = loader.loadXMLContent(NContext.current.getDefaultLangFilePath());
		load.mainFont = loader.loadFontGenerator("roboreg");
        load.sansFont = loader.loadFontGenerator("digits");
		load.common = loader.loadXMLContent(NContext.current.getCommonContentFilePath());
//		NCore.instance().getSoundman().initSoundsLoading();
	}
	
    protected int maxTextureSize=0;

    protected BitmapFont addFont(FreeTypeFontGenerator gen, String fontName, String chars, String fallbackchars, int border, boolean whiteBorder)
    {
        if(maxTextureSize == 0)
        {
            IntBuffer buf = BufferUtils.newIntBuffer(16);
            Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
            maxTextureSize = buf.get(0);
        }

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = NContext.current.iScale(common.getNodeValueInt("font_" + fontName));
        parameter.characters = chars;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.flip = false;

        if(border > 0)
        {
            parameter.borderColor = whiteBorder ? new Color(1,1,1,0.5f) : new Color(0, 0, 0, 0.6f);
            parameter.borderWidth = border;
        }

        BitmapFont fnt = gen.generateFont(parameter);
        if(maxTextureSize > 0 && fnt.getRegion().getTexture().getWidth() > maxTextureSize && fallbackchars != null)
        {
            //fallback mode - we do not loaded our font
            fnt.dispose();
            parameter.characters = fallbackchars;
            fnt = gen.generateFont(parameter);
            fallbackENOnly = true;
        }
//        fnt.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fonts.put(fontName, fnt);
        return fnt;
    }

    protected static final String DIGITS_ONLY = "0123456789+-.,()[]?|";
    protected static final String CHARS_CYR_ONLY =  "\u0410\u0411\u0412\u0413\u0414\u0415\u0401\u0416\u0417\u0418\u0419\u041A\u041B\u041C\u041D\u041E\u041F\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042C\u042B\u042A\u042D\u042E\u042F\u0430\u0431\u0432\u0433\u0434\u0435\u0451\u0436\u0437\u0438\u0439\u043A\u043B\u043C\u043D\u043E\u043F\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044C\u044B\u044A\u044D\u044E\u044F";
    protected static final String DEFAULT_CHARS_CYR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*¢"
            //russian characters here:
            + CHARS_CYR_ONLY
            ;
    protected static final String DEFAULT_CHARS_EU = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*¢\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF"
            //polish characters here:
            + "\u015B\u0107\u0119\u0142\u0105\u017C\u0179\u0144\u0141"
            ;
    protected static final String DEFAULT_CHARS_EN_ONLY = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*¢";
    public void endLoadingPart2()
	{
		IResLoader loader = ResLoader.singleton();

        common = loader.getLoaded(load.common);

        String fontChars;

        switch (NContext.current.getLangGroup())
        {
            case NContext.LANG_GROUP_CYR:
                fontChars = DEFAULT_CHARS_CYR;
                break;
            case NContext.LANG_GROUP_EU:
                fontChars = DEFAULT_CHARS_EU;
                break;
            default:
                fontChars = DEFAULT_CHARS_EN_ONLY;
        }

		FreeTypeFontGenerator gen = loader.getLoaded(load.mainFont);
        String allChars = DEFAULT_CHARS_EU + CHARS_CYR_ONLY;
		addFont(gen, "m20", allChars, DEFAULT_CHARS_EN_ONLY, 0, false);
		addFont(gen, "m30", allChars, DEFAULT_CHARS_EN_ONLY, 0, false);
		if(defaultFont != null)
			defaultFont.dispose();

		defaultFont = addFont(gen, "def", fontChars, DEFAULT_CHARS_EN_ONLY, 0, false);

        loader.unload(load.mainFont);
        gen = loader.getLoaded(load.sansFont);

        addFont(gen, "d40", DIGITS_ONLY, null, 0, false);
        addFont(gen, "d40b", DIGITS_ONLY, null, 1, false);
        addFont(gen, "d20", DIGITS_ONLY, null, 0, false);
		addFont(gen, "d70", DIGITS_ONLY, null, 0, false);

        loader.unload(load.sansFont);

		buildI18N(load);

		loadColors(common);
//		NCore.instance().getSoundman().finishSoundsLoading();
        NContext.current.subRefresh();
	}

    protected boolean fallbackENOnly = false;

	public void buildI18N(LoadIds load)
	{
		IResLoader loader = ResLoader.singleton();
		i18n = loader.getLoaded(load.i18n);
		{
			Set<Entry<String, XMLContentLoader.Node>> eset = i18n.nameNodeMap.entrySet();
			for(Entry<String, XMLContentLoader.Node> ent : eset)
			{
				ent.getValue().value = ent.getValue().value.replace("\\n", "\n");
			}

			if(load.i18nlocal != -1 && !fallbackENOnly)
			{
				XMLContentLoader local = loader.getLoaded(load.i18nlocal);

				Set<Entry<String, XMLContentLoader.Node>> esetLocal = local.nameNodeMap.entrySet();
				for(Entry<String, XMLContentLoader.Node> ent : esetLocal)
				{
					ent.getValue().value = ent.getValue().value.replace("\\n", "\n");
					i18n.nameNodeMap.put(ent.getKey(), ent.getValue());
				}

				loader.unload(load.i18nlocal);
			}
		}
	}

	public void reloadLang(String lang)
	{
		NContext.current.setLang(lang);
		IResLoader loader = ResLoader.singleton();
        String langPath = NContext.current.getLangFilePath();
        load.i18nlocal = -1;
        if(!NContext.current.getDefaultLangFilePath().equals(langPath))
        	load.i18nlocal = loader.loadXMLContent(langPath);
        load.i18n = loader.loadXMLContent(NContext.current.getDefaultLangFilePath());
		loader.finishLoading();
		buildI18N(load);
	}

	/**
	 * Translate message from name to local loaded translation
	 * @param from
	 * @return
	 */
	public String x(String from)
	{
		if(from == null || i18n == null || i18n.nameNodeMap == null)
			return from;

		XMLContentLoader.Node node = i18n.nameNodeMap.get(from);
		if(node == null || node.value == null)
			return from;
		return node.value;
	}

	public BitmapFont fontDefault()
	{
		return defaultFont;
	}

	protected Color defaultColorFG = Color.valueOf("1C16D3");

	public Color colorDefaultFG()
	{
		return defaultColorFG;
	}

	public BitmapFont font(String fontName)
	{
		return fonts.get(fontName);
	}

	protected Map<String, Color> colors = new HashMap<String, Color>();

	public Color getColor(String colorName)
	{
		if(colorName.startsWith("#"))
		{
			return Color.valueOf(colorName.substring(1));
		}
		return colors.get(colorName);
	}

	protected void loadColors(XMLContentLoader loader)
	{
		XMLContentLoader.Node node = loader.nameNodeMap.get("colors");
		node = node.child;
		while(node != null)
		{
			XMLContentLoader.Node.Attrib name = node.attr.get("name");
			XMLContentLoader.Node.Attrib value = node.attr.get("value");

			node = node.sibling;

			if(name == null || value == null)
				continue;

			colors.put(name.getValue(), Color.valueOf(value.getValue()));
		}
	}

	public XMLContentLoader getCommon()
	{
		return common;
	}
	
	protected TextureAtlas sharedAtlas;
	
	/**
	 * Sets shared atlas used for board properties
	 * @param atlas
	 */
	public void setSharedAtlas(TextureAtlas atlas)
	{
		sharedAtlas = atlas;
	}
	
	public void resetSharedAtlas()
	{
		sharedAtlas = null;
	}
	
	public TextureRegion getDrawable(String name)
	{
		TextureRegion reg = sharedAtlas.findRegion(name);
		return reg;
	}
	
	public Sprite newSprite(String name)
	{
		return sharedAtlas.createSprite(name);
	}
	
	public NinePatch newNinePatch(String name)
	{
		return sharedAtlas.createPatch(name);
	}
	
	public TextureAtlas getCommonAtlas()
	{
		return commonAtlas;
	}
}
