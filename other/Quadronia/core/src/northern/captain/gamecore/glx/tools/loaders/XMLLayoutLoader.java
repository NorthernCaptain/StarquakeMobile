package northern.captain.gamecore.glx.tools.loaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.effect.ParticleEffectActor;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.setup.SetupVisual;
import northern.captain.gamecore.glx.tools.*;

import northern.captain.tools.Log;
import northern.captain.tools.StringUtils;

public class XMLLayoutLoader extends XMLContentLoader
{
    /**
     * Delta shift of our scene against the real screen
     * We use this if the screen is wider than the scene
     */
    int deltaScreenX;
    int deltaScreenY;

	public XMLLayoutLoader()
	{
		super(true);
	}

    public int getDeltaScreenX()
    {
        return deltaScreenX;
    }

    public void setDeltaScreenX(int deltaScreenX)
    {
        this.deltaScreenX = deltaScreenX;
    }

    public int getDeltaScreenY()
    {
        return deltaScreenY;
    }

    public void setDeltaScreenY(int deltaScreenY)
    {
        this.deltaScreenY = deltaScreenY;
    }

    public Sprite newSprite(String name, TextureAtlas atlas)
    {
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            Log.w("ncgame", "SPRITE NOT FOUND: " + name);
//            throw new IllegalArgumentException("No sprite with name: " + name);
            return null;
        }

        String imageName = getAttribString(node, "img", name);

        TextureAtlas.AtlasRegion region = atlas.findRegion(imageName);
        region = cutTextureRegion(region, node);
        Sprite sprite = NContext.current.newSprite(region);
        setSize(sprite, node);
        setPosition(sprite, node);
        sprite.setAlpha(getAttribFloat(node, "alpha", 1.0f));
        return sprite;
    }

    public String getFullName(String name)
    {
        return getFullName(name, NContext.current.getLang());
    }

    public String getFullName(String name, String lang)
    {
        String fullname = name + lang;
        if(nameNodeMap.containsKey(fullname))
        {
            return fullname;
        }
        return name + "en";
    }

	public Image newImage(String name, TextureAtlas atlas)
	{
		
		XMLContentLoader.Node node = nameNodeMap.get(name);
		if(node == null)
		{
			return null;
		}

		String imageName = getAttribString(node, "img", name);
        BaseDrawable drawable = getDrawable(imageName, node, atlas, true);

        if(drawable == null)
            return null;

		Image img;
        img = new Image(drawable);
        float width = NContext.current.fScale(img.getWidth());
        float height = NContext.current.fScale(img.getHeight());
        if(drawable.getClass() == TextureRegionDrawable.class)
        {
            img.setScaling(NContext.current.getImageScaling());
            drawable.setMinWidth(width);
            drawable.setMinHeight(height);
        }

        img.setSize(width, height);
        img.setOrigin(NContext.current.fScale(img.getOriginX()), NContext.current.fScale(img.getOriginY()));

		setSize(img, node);
		setPosition(img, node);
        setOrigin(img, node);
		img.setName(name);
		
		return img;
	}

    public String getParticleEffectName(String name)
    {
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            return null;
        }

        return getAttribString(node, "effect", null);
    }

    public ParticleEffectActor newEffectActor(String name, ParticleEffect effect, TextureAtlas atlas)
    {
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            return null;
        }

        ParticleEffectActor actor = new ParticleEffectActor(effect);

        setSize(actor, node);
        setPosition(actor, node);
        actor.setEffectPosition((int)actor.getX(), (int)actor.getY());
        setOrigin(actor, node);
        actor.setName(name);

        actor.loadSprites(atlas);

        return actor;
    }

    public ParticleEffect duplicateEffect(ParticleEffect original)
    {
        ParticleEffect copy = new ParticleEffect(original);

        Array<ParticleEmitter> oemitters = original.getEmitters();
        Array<ParticleEmitter> cemitters = copy.getEmitters();

        for(int i=0;i<oemitters.size;i++)
        {
            cemitters.get(i).setImagePaths(oemitters.get(i).getImagePaths());
        }

        return copy;
    }

    public IndexImage newIndexImage(String name, TextureAtlas atlas)
    {

        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            return null;
        }

        String imageName = getAttribString(node, "img", name);
        BaseDrawable drawable = getDrawable(imageName, node, atlas);

        String type = getAttribString(node, "type", "plain");
        IndexImage img;
        img = new IndexImage(drawable);
        if(drawable.getClass() == TextureRegionDrawable.class)
            img.setScaling(NContext.current.getImageScaling());

        setSize(img, node);
        setPosition(img, node);
        setOrigin(img, node);
        img.setName(name);

        return img;
    }

    public ScrollPane newScrollPane(String name)
	{
		XMLContentLoader.Node node = nameNodeMap.get(name);
		if(node == null)
		{
			return new ScrollPane(null);
		}
		ScrollPane pane = new ScrollPane(null);
		setSize(pane, node);
		setPosition(pane, node);
		setOrigin(pane, node);
		pane.setName(name);
		return pane;
	}
	
	public ScrollTable newScrollTable(String name, TextureAtlas atlas)
	{
		XMLContentLoader.Node node = nameNodeMap.get(name);
		if(node == null)
		{
			return new ScrollTable(null);
		}
		ScrollTable pane = new ScrollTable(null);

        if(node.attr.containsKey("vscroll"))
        {
            ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
            if(node.attr.containsKey("vdota"))
            {
                Drawable active = getDrawable(getAttribString(node, "vdota", null), atlas);
                Drawable inactive = getDrawable(getAttribString(node, "vdoti", null), atlas);
                VScrollKnobDrawable knobDrawable = new VScrollKnobDrawable(active, inactive,
                    getAttribInt(node, "vdotdelta", 0));
                Drawable arrowUpActive = getDrawable(getAttribString(node, "varupac", null), atlas);
                Drawable arrowUpInactive = getDrawable(getAttribString(node, "varupin", null), atlas);
                Drawable arrowDnActive = getDrawable(getAttribString(node, "vardnac", null), atlas);
                Drawable arrowDnInactive = getDrawable(getAttribString(node, "vardnin", null), atlas);
                VScrollDrawable vScrollDrawable = new VScrollDrawable(
                    arrowUpActive, arrowUpInactive, arrowDnActive, arrowDnInactive,
                    knobDrawable);
                style.vScroll = vScrollDrawable;
                style.vScrollKnob = knobDrawable;
            } else
            {
                style.vScroll = getDrawable(getAttribString(node, "vscroll", null), atlas);
                style.vScrollKnob = getDrawable(getAttribString(node, "vknob", null), atlas);
            }
            pane.setStyle(style);
        }

		setSize(pane, node);
		setPosition(pane, node);
		setOrigin(pane, node);
		pane.setName(name);
		return pane;		
	}

	public MyLabel newLabelNullable(String name)
    {
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            return null;
        }
        return newLabel(name);
    }

	public MyLabel newLabel(String name)
	{
		LabelStyle style = new LabelStyle();
		MyLabel lbl = null;
		XMLContentLoader.Node node = nameNodeMap.get(name);
		if(node == null)
		{
			style.font = SharedRes.instance.fontDefault();
			style.fontColor = SharedRes.instance.colorDefaultFG();

			lbl = new MyLabel(null, style);
			lbl.setName(name);
			return lbl;
		}

		String fontname = getAttribString(node, "font", null);
		if(fontname != null)
		{
			style.font = SharedRes.instance.font(fontname);
		}
		
		style.fontColor = getAttribColor(node, "fcolor", SharedRes.instance.colorDefaultFG());

		String text = SharedRes.instance.x(getAttribString(node, "text", null));
		
		lbl = new MyLabel(text, style);
		lbl.setName(name);
		
		setPosition(lbl, node);
		setSize(lbl, node);
		setOrigin(lbl, node);
		lbl.setAlignment(getAlign(node));
        float fscale = getAttribFloat(node, "fscale", 1.0f);
        if(fscale != 1.0f)
            lbl.setFontScale(fscale);
		return lbl;
	}
	
	public Table newTextButton(String name, TextureAtlas atlas)
	{
		MyTextButton.TextButtonStyle style = new MyTextButton.TextButtonStyle();
		MyTextButton lbl = null;
		
		XMLContentLoader.Node node = nameNodeMap.get(name);
		if(node == null)
		{
			style.font = SharedRes.instance.fontDefault();
			style.fontColor = SharedRes.instance.colorDefaultFG();
			
			lbl = new MyTextButton(null, style);
			lbl.setName(name);
			Table tbl = new Table();
			tbl.add(lbl).expand().fill();
			return tbl;
		}

		String fontname = getAttribString(node, "font", null);
		if(fontname != null)
		{
			style.font = SharedRes.instance.font(fontname);
		}
		
		style.fontColor = getAttribColor(node, "fcolor", SharedRes.instance.colorDefaultFG());

		String text = SharedRes.instance.x(getAttribString(node, "text", null));

        style.textPadBottom = NContext.current.fScale(getAttribFloat(node, "text_y", 0));

		String imageName = getAttribString(node, "img", name);
        BaseDrawable drawable = getDrawable(imageName, node, atlas);

		style.up = drawable;
		lbl = new MyTextButton(text, style);
		lbl.setName(name);
		lbl.setSize(drawable.getMinWidth(), drawable.getMinHeight());
		
		setPosition(lbl, node);
		setSize(lbl, node);
		setOrigin(lbl, node);
//		lbl.setTransform(true);
		lbl.getLabel().setAlignment(getAlign(node));
		lbl.getLabelCell().maxHeight(drawable.getMinHeight());
		lbl.getLabelCell().maxWidth(drawable.getMinWidth());

        float fscale = getAttribFloat(node, "fscale", 1.0f);
        if(fscale != 1.0f)
            lbl.setFontScale(fscale);

		Table tbl = new Table();
		tbl.setSize(drawable.getMinWidth(), drawable.getMinHeight());
		setPosition(tbl, node);
		setSize(tbl, node);
		setOrigin(tbl, node);
//		tbl.setTransform(true);
		tbl.add(lbl).expand().fill().width(tbl.getWidth()).height(tbl.getHeight());
		return tbl;
	}

    public static Label buttonLabel(Table button)
    {
        Actor actor = button.getChildren().get(0);
        if(actor instanceof MyTextButton)
        {
            return ((MyTextButton)actor).getLabel();
        }
        return null;
    }

    public ButtonImage newImageButtonWithTextAndBG(String name, TextureAtlas atlas, TextureAtlas bgAtlas)
    {
        ButtonImage otbl = new ButtonImage();
        Table tbl = otbl;
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            return null;
        }

        String imageName = getAttribString(node, "bg", null);
        BaseDrawable drawable;
        if(imageName != null)
        {
            drawable = getDrawable(imageName, node, bgAtlas);
            tbl.setBackground(drawable);
            tbl.setSize(drawable.getMinWidth(), drawable.getMinHeight());
        }

        setSize(tbl, node);
        setOrigin(tbl, node);
        setPosition(tbl, node);

        imageName = getAttribString(node, "img", name);
        TextureAtlas.AtlasRegion reg = atlas.findRegion(imageName);

        reg = cutTextureRegion(reg, node);

        Image img = new Image(reg);
        img.setScaling(NContext.current.getImageScaling());
        img.setSize(NContext.current.fScale(reg.getRegionWidth()),
            NContext.current.fScale(reg.getRegionHeight()));

        tbl.add(img).center().left().width(img.getWidth()).height(img.getHeight());

        otbl.setButtonImage(img);

        MyLabel.LabelStyle style = new MyLabel.LabelStyle();
        MyLabel lbl = null;

        String fontname = getAttribString(node, "font", null);
        if(fontname != null)
        {
            style.font = SharedRes.instance.font(fontname);
        }

        style.fontColor = getAttribColor(node, "fcolor", SharedRes.instance.colorDefaultFG());

        String text = SharedRes.instance.x(getAttribString(node, "text", null));

        float padX = NContext.current.fScale(getAttribFloat(node, "text_x", 0));

        lbl = new MyLabel(text, style);
        lbl.setName(name);

//		lbl.setTransform(true);
        lbl.setAlignment(getAlign(node));
        lbl.setWidth(otbl.getWidth() - img.getWidth());
//        lbl.getLabelCell().maxHeight(drawable.getMinHeight());
//        lbl.getLabelCell().maxWidth(drawable.getMinWidth());

        float fscale = getAttribFloat(node, "fscale", 1.0f);
        if(fscale != 1.0f)
            lbl.setFontScale(fscale);


        otbl.add(lbl).expand().fillX().center().left().padLeft(padX).width(lbl.getWidth());

        otbl.setTouchable(Touchable.enabled);

        return otbl;
    }

    public ButtonImage newImageButtonWithBG(String name, TextureAtlas atlas, TextureAtlas bgAtlas)
    {
    	Table tbl = new Table();
		XMLContentLoader.Node node = nameNodeMap.get(name);
		if(node == null)
		{			
			return null;
		}

		String imageName = getAttribString(node, "bg", name);
		BaseDrawable drawable = getDrawable(imageName, node, bgAtlas);
		tbl.setBackground(drawable);
		tbl.setSize(drawable.getMinWidth(), drawable.getMinHeight());
		
		setSize(tbl, node);
		setOrigin(tbl, node);
		setPosition(tbl, node);
		
		imageName = getAttribString(node, "img", name);
		TextureAtlas.AtlasRegion reg = atlas.findRegion(imageName);

        reg = cutTextureRegion(reg, node);

		Image img = new Image(reg);			
		img.setScaling(NContext.current.getImageScaling());
		img.setSize(NContext.current.fScale(reg.getRegionWidth()), 
					NContext.current.fScale(reg.getRegionHeight()));

		tbl.add(img).center().width(img.getWidth()).height(img.getHeight());

        ButtonImage otbl = new ButtonImage();
        otbl.setButtonImage(img);
        otbl.setSize(drawable.getMinWidth(), drawable.getMinHeight());
        setPosition(otbl, node);
        setSize(otbl, node);
        setOrigin(otbl, node);
//		tbl.setTransform(true);
        otbl.add(tbl).expand().fill().width(tbl.getWidth()).height(tbl.getHeight());
        otbl.setTouchable(Touchable.enabled);

    	return otbl;
    }

    public void applyAttributes(Actor actor, String nodeName)
    {
        XMLContentLoader.Node node = nameNodeMap.get(nodeName);
        if(node == null)
        {
            return;
        }
        setSize(actor, node);
        setOrigin(actor, node);
        setPosition(actor, node);
    }

	public ButtonImage newImageButton(String name, TextureAtlas atlas)
	{
        Table tbl = new Table();
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            return null;
        }

        String imageName = getAttribString(node, "img", name);
        BaseDrawable drawable = getDrawable(imageName, node, atlas, true);
        tbl.setSize(drawable.getMinWidth(), drawable.getMinHeight());

        Image img = new Image(drawable);
        img.setScaling(NContext.current.getImageScaling());
        img.setSize(NContext.current.fScale(img.getWidth()),
                NContext.current.fScale(img.getHeight()));

        setSize(tbl, node);
        setOrigin(tbl, node);
        setPosition(tbl, node);

        tbl.add(img).center().width(img.getWidth()).height(img.getHeight());

        ButtonImage otbl = new ButtonImage();
        otbl.setButtonImage(img);
        otbl.setSize(drawable.getMinWidth(), drawable.getMinHeight());
        setPosition(otbl, node);
        setSize(otbl, node);
        setOrigin(otbl, node);
//		tbl.setTransform(true);
        otbl.add(tbl).expand().fill().width(tbl.getWidth()).height(tbl.getHeight());
        otbl.setTouchable(Touchable.enabled);

        return otbl;
	}
	
	public Window newWindow(String name, TextureAtlas atlas)
	{
		WindowStyle style = new WindowStyle();
		XMLContentLoader.Node node = nameNodeMap.get(name);
		if(node == null)
		{
			Log.w("ncgame", "Could not find Window xml tag: " + name);
			return null;
		}
		
		String fontname = getAttribString(node, "font", null);
		if(fontname != null)
		{
			style.titleFont = SharedRes.instance.font(fontname);
		}
		
		style.titleFontColor = getAttribColor(node, "fcolor", SharedRes.instance.colorDefaultFG());
		
		String bgName = getAttribString(node, "bg", null);
        style.background = getDrawable(bgName, node, atlas);

		String title = getAttribString(node, "title", name);
		
		Window win = new Window(SharedRes.instance.x(title), style);
		setSize(win, node);
		setPosition(win, node);
		setOrigin(win, node);
		
		Color color = getAttribColor(node, "color", null);
		if(color != null)
			win.setColor(color);
		
		return win;
	}

    public BaseDrawable getDrawable(String name, TextureAtlas atlas)
    {
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            Log.w("ncgame", "Could not find Drawable xml tag: " + name);
            return null;
        }
        String imageName = getAttribString(node, "img", name);
        return getDrawable(imageName, node, atlas);
    }

    public BaseDrawable getDrawable(String imageName, Node node, TextureAtlas atlas)
    {
        String type = getAttribString(node, "type", "plain");
        return getDrawable(imageName, node, atlas, type, false);
    }

    public BaseDrawable getDrawable(String imageName, Node node, TextureAtlas atlas, boolean needCut)
    {
        String type = getAttribString(node, "type", "plain");
        return getDrawable(imageName, node, atlas, type, needCut);
    }

    public BaseDrawable getDrawable(String imageName, Node node, TextureAtlas atlas, String type)
    {
        return getDrawable(imageName, node, atlas, type, false);
    }
    public BaseDrawable getDrawable(String imageName, Node node, TextureAtlas atlas, String type, boolean needCut)
    {
        if(imageName != null)
        {
            if(type.equals("tile"))
            {
                TextureAtlas.AtlasRegion region = atlas.findRegion(imageName);
                if(region == null)
                    return null;

                if(needCut)
                    region = cutTextureRegion(region, node);
                TiledDrawable2 drawable = new TiledDrawable2(region);
                drawable.setScaleFactor(getAttribFloat(node, "scale", NContext.current.getScaleFactor()))
                        .setDelta(
                                NContext.current.fScale(getAttribFloat(node, "dx", 0)),
                                NContext.current.fScale(getAttribFloat(node, "dy", 0)));
//                drawable.setMinWidth(getAttribFloat(node, "dw", drawable.getMinWidth()));
//                drawable.setMinHeight(getAttribFloat(node, "dh", drawable.getMinHeight()));
                return drawable;
            }
            if(type.equals("nine"))
            {
                NinePatch ninePatch = atlas.createPatch(imageName);
                if(ninePatch == null)
                    return null;

                NinePatchDrawable drawable = new NinePatchDrawable(ninePatch);
                drawable.setMinWidth(NContext.current.fScale(getAttribFloat(node, "dw", drawable.getMinWidth())));
                drawable.setMinHeight(NContext.current.fScale(getAttribFloat(node, "dh", drawable.getMinHeight())));
                return drawable;
            }
            {
                TextureAtlas.AtlasRegion reg = atlas.findRegion(imageName);
                if(reg == null)
                    return null;

                if(needCut)
                    reg = cutTextureRegion(reg, node);
                TextureRegionDrawable drawable = new TextureRegionDrawable(reg);
                return drawable;
            }
        }

        return null;
    }

    public TextField newTextField(String name, TextureAtlas atlas)
    {
        TextField fld = null;
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            Log.w("ncgame", "Could not find TextField xml tag: " + name);
            return null;
        }

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        String imageName = getAttribString(node, "img", null);

        style.background = getDrawable(imageName, node, atlas);

        imageName = getAttribString(node, "cursor", "v3_input_cursor");

        style.cursor = getDrawable(imageName, node, atlas, "plain");

        String fontname = getAttribString(node, "font", null);
        if(fontname != null)
        {
            style.font = SharedRes.instance.font(fontname);
        }

        style.fontColor = getAttribColor(node, "fcolor", SharedRes.instance.colorDefaultFG());


        String text = SharedRes.instance.x(getAttribString(node, "text", ""));
        fld = new TextField(text, style);

        setSize(fld, node);
        setPosition(fld, node);
        setOrigin(fld, node);

        float fscale = getAttribFloat(node, "fscale", 1.0f);
//        if(fscale != 1.0f)
//            fld.setScaleFactor(fscale);

        return fld;
    }

    public <T extends Actor> T applyTo(T actor, String name)
    {
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            Log.w("ncgame", "Could not find actor xml tag: " + name);
            return actor;
        }

        setSize(actor, node);
        setOrigin(actor, node);
        setPosition(actor, node);

        return actor;
    }

    public Table newTable(String name, TextureAtlas atlas)
    {
        Table table = new Table();

        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            Log.w("ncgame", "Could not find Window xml tag: " + name);
            return null;
        }

        String imageName = getAttribString(node, "img", null);

        if(atlas != null && !StringUtils.isNullOrEmpty(imageName))
        {
            BaseDrawable drawable = getDrawable(imageName, node, atlas, true);
            table.setBackground(drawable);
            table.setSize(drawable.getMinWidth(), drawable.getMinHeight());
        }

        setSize(table, node);
        setPosition(table, node);
        setOrigin(table, node);

        return table;
    }

    public SetupVisual newSetupVisual(String name, TextureAtlas atlas)
    {
        XMLContentLoader.Node node = nameNodeMap.get(name);
        if(node == null)
        {
            return null;
        }

        String imageName = getAttribString(node, "img", name);
        BaseDrawable drawable = getDrawable(imageName, node, atlas);

        SetupVisual.SetupVisualStyle style = new SetupVisual.SetupVisualStyle();

        style.mainImage = drawable;

        imageName = getAttribString(node, "offimg", null);
        if(imageName != null)
        {
            style.offImage = getDrawable(imageName, node, atlas);
        }

        imageName = getAttribString(node, "bg", null);
        if(imageName != null)
        {
            style.bgImage = getDrawable(imageName, node, atlas);
        }

        style.mainText = getAttribString(node, "text", null);
        style.mainText = style.mainText == null ? null : SharedRes.instance.x(style.mainText);
        style.offText = getAttribString(node, "offtext", null);
        style.offText = style.offText == null ? null : SharedRes.instance.x(style.offText);


        Label lbl = newLabel(name);

        SetupVisual vis = new SetupVisual(style, lbl);

        Image img = vis.getImage();
        if(drawable.getClass() == TextureRegionDrawable.class)
            img.setScaling(NContext.current.getImageScaling());

        setSize(img, node);
        setPosition(img, node);
        setOrigin(img, node);

        vis.create();

        return vis;
    }


	public final int getAttribInt(XMLContentLoader.Node node, String name, int defaultVal)
	{
		XMLContentLoader.Node.Attrib att = node.attr.get(name);
		if(att == null)
			return defaultVal;
		return att.getValueInt();
	}
	
	public final float getAttribFloat(XMLContentLoader.Node node, String name, float defaultVal)
	{
		XMLContentLoader.Node.Attrib att = node.attr.get(name);
		if(att == null)
			return defaultVal;
		return att.getValueFloat();
	}

	public final String getAttribString(XMLContentLoader.Node node, String name, String defaultVal)
	{
		XMLContentLoader.Node.Attrib att = node.attr.get(name);
		if(att == null)
			return defaultVal;
		return att.getValue();
	}
		
	protected final Color getAttribColor(XMLContentLoader.Node node, String name, Color defaultVal)
	{
		XMLContentLoader.Node.Attrib att = node.attr.get(name);
		if(att == null)
			return defaultVal;
		return SharedRes.instance.getColor(att.getValue());
	}

    public TextureAtlas.AtlasRegion cutTextureRegion(TextureAtlas.AtlasRegion region, Node node)
    {
        int dx = NContext.current.iScale(getAttribInt(node, "imx", 0));
        int dy = NContext.current.iScale(getAttribInt(node, "imy", 0));
        int tw = NContext.current.iScale(getAttribInt(node, "imw", 0));
        int th = NContext.current.iScale(getAttribInt(node, "imh", 0));

        int flip = getAttribInt(node, "imflip", 0);

        if(dx != 0 || dy != 0 || tw != 0 || th != 0 || flip != 0)
        {
            region = new TextureAtlas.AtlasRegion(region);

            if(dx != 0)
            {
                region.setRegionX(region.getRegionX() + dx);
            }

            if(dy != 0)
            {
                region.setRegionY(region.getRegionY() + dy);
            }

            if(tw != 0)
            {
                region.setRegionWidth(tw);
            }

            if(th != 0)
            {
                region.setRegionHeight(th);
            }

            if(flip > 0)
            {
                region.flip((flip & 1) == 1, (flip & 2) == 2);
            }

        }
        return region;
    }

	public void setPosition(Actor obj, XMLContentLoader.Node node)
	{
        int x = NContext.current.iScale(getAttribInt(node, "x", 0));
        if(x < 0)
        {
            x = NContext.current.screenWidth + x + deltaScreenX;
        } else
        {
            x += deltaScreenX;
        }

        int y = NContext.current.iScale(getAttribInt(node, "y", 0));
        if(y < 0)
        {
            y = NContext.current.screenHeight + y + deltaScreenY;
        } else
        {
            y += deltaScreenY;
        }
        if(getAttribString(node, "align", "left").equals("center"))
        {
            obj.setPosition(x - obj.getWidth()/2.0f, y - obj.getHeight()/2.0f);
        } else
        {
            obj.setPosition(x,y);
        }
	}

    public void setPosition(Sprite obj, XMLContentLoader.Node node)
    {
        int x = NContext.current.iScale(getAttribInt(node, "x", 0));
        if(x < 0)
        {
            x = NContext.current.screenWidth + x + deltaScreenX;
        } else
        {
            x += deltaScreenX;
        }

        int y = NContext.current.iScale(getAttribInt(node, "y", 0));
        if(y < 0)
        {
            y = NContext.current.screenHeight + y + deltaScreenY;
        } else
        {
            y += deltaScreenY;
        }

        if(getAttribString(node, "align", "left").equals("center"))
        {
            obj.setCenter(x,y);
//            obj.setOrigin(obj.getWidth()/2, obj.getHeight()/2);
        } else
        {
            obj.setPosition(x,y);
        }

        float scale = getAttribFloat(node, "scale", 1);
        obj.setScale(scale);
    }

    public void setSize(Sprite obj, XMLContentLoader.Node node)
    {
        float w = getAttribFloat(node, "w", 0);
        w = w != 0 ? NContext.current.fScale(w) : obj.getWidth();

        float h = getAttribFloat(node, "h", 0);
        h = h != 0 ? NContext.current.fScale(h) : obj.getHeight();
        obj.setSize(w, h);
    }


	public void setSize(Actor obj, XMLContentLoader.Node node)
	{
        float w = getAttribFloat(node, "w", 0);
        w = w != 0 ? NContext.current.fScale(w) : obj.getWidth();

        float h = getAttribFloat(node, "h", 0);
        h = h != 0 ? NContext.current.fScale(h) : obj.getHeight();
		obj.setWidth(w);
		obj.setHeight(h);
	}
	
	public void setOrigin(Actor obj, XMLContentLoader.Node node)
	{
		String originx = getAttribString(node, "orx", "center");
		if("center".equals(originx))
		{
			obj.setOriginX(obj.getWidth()/2.0f);
		}
		else
			if("right".equals(originx))
			{
				obj.setOriginX(obj.getWidth());
			}
			else
            {
                if("left".equals(originx))
                {
				    obj.setOriginX(0);
                } else
                {
                    try
                    {
                        float origX = NContext.current.fScale(Float.parseFloat(originx));
                        obj.setOriginX(origX);
                    } catch(Exception ex)
                    {
                        obj.setOriginX(0);
                    }
                }

            }

		String originy = getAttribString(node, "ory", "center");
		if("center".equals(originy))
		{
			obj.setOriginY(obj.getHeight()/2.0f);
		} else
			if("down".equals(originy))
			{
				obj.setOriginY(obj.getHeight());
			}
			else
				obj.setOriginY(0);
	}

	protected int getAlign(XMLContentLoader.Node node)
	{
		String originx = getAttribString(node, "alignx", "left");
		int align = 0;
		if("center".equals(originx))
		{
			align |= Align.center;
		}
		else
			if("right".equals(originx))
			{
				align |= Align.right;
			}
			else
				align |= Align.left;
		
		String originy = getAttribString(node, "aligny", "center");
		if("center".equals(originy))
		{
			align |= Align.center;
		} else
			if("down".equals(originy))
			{
				align |= Align.bottom;
			}
			else
				align |= Align.top;
		return align;
	}
	
	public <T extends Actor> T addToCell(Table table, T actor)
	{
		Cell<T> cell = table.add(actor);
		cell.width(actor.getWidth());
		cell.height(actor.getHeight());
		
		float dx = actor.getX();
		float dy = actor.getY();
		
		if(dx != 0)
		{
			if(dx > 0)
				cell.padLeft(dx);
			else
				cell.padRight(-dx);
			actor.setX(0);
		}
		
		if(dy != 0)
		{
			if(dy>0)
				cell.padTop(dy);
			else
				cell.padBottom(-dy);
			actor.setY(0);
		}
		return actor;
	}

	public <T extends Actor> T addToCellTiled(Table table, T actor)
	{
		Cell<T> cell = table.add(actor);
		cell.minWidth(actor.getWidth());
		cell.minHeight(actor.getHeight());
		
		float dx = actor.getX();
		float dy = actor.getY();
		
		if(dx != 0)
		{
			if(dx > 0)
				cell.padLeft(dx);
			else
				cell.padRight(-dx);
			actor.setX(0);
		}
		
		if(dy != 0)
		{
			if(dy>0)
				cell.padTop(dy);
			else
				cell.padBottom(-dy);
			actor.setY(0);
		}
		return actor;
	}
}
