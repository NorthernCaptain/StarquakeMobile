package northern.captain.gamecore.glx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.screens.IScreenActivator;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenWorkflow;
import northern.captain.gamecore.glx.shader.CustomShader;
import northern.captain.gamecore.glx.shader.ShaderFactory;
import northern.captain.gamecore.glx.tools.Message;
import northern.captain.gamecore.glx.tools.MySpriteBatch;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.tools.Log;

import java.util.LinkedList;

public class NContext
{
	protected static float modelRatio = 1.65f;

//	protected static final String[] gfxNames = { "gfx480", "gfx720" };
//	protected static final int[] gfxWidths = { 480, 720 };
    protected static final String[] gfxNames = { "gfx1080" };
    protected static final int[] gfxWidths = { 1080 };

	public static NContext current;

	protected float factor = 1.0f;
	public boolean needFactor = false;

	public int screenWidth;
	public int screenHeight;
	public int centerX;
	public int centerY;

	public int gameAreaHeight;
	public int gameAreaDeltaY;
	public int gameAreaDeltaX;

	public int modelWidth;
	public String modelGfxName;

	protected String gfxDir;

	public int statMaxSpritesInBatch;
	public int statRederCalls;

	public Stage currentStage;

    public Stack gameFrame;

    public OrthographicCamera camera;
    public ScreenViewport viewport;

    public int  cameraAngle = 0;

	protected int modelLen, sceneLen;

	public SpriteBatch batch;

    private boolean useHeader = true;

    private boolean useFooter = true;

    private boolean allowStatusBar = true;

    private boolean continuousRender = true;

	public NContext()
	{

	}

	public String getPackageCodePath()
	{
		return ".";
	}

	public void setCurrentStage(Stage stage)
	{
		currentStage = stage;
        if(camera == null)
        {
            camera = (OrthographicCamera)stage.getCamera();
            viewport = (ScreenViewport) stage.getViewport();
            if(cameraAngle != 0)
            {
                camera.rotate(cameraAngle, 0, 0, 1);
            }
        }
	}

	public void unsetCurrentStage(Stage stage)
	{
		if (currentStage == stage)
		{
			currentStage = null;
            gameFrame = null;
		}
	}


    public void setGameFrame(Stack frame)
    {
        gameFrame = frame;
    }

	public Actor findActor(String name)
	{
		if(currentStage == null)
			return null;
		Array<Actor> actors = currentStage.getActors();
		for(Actor actor : actors)
		{
			if(name.equals(actor.getName()))
			{
				return actor;
			}
		}
		return null;
	}
	
	public void updateStats(SpriteBatch batch)
	{
		statMaxSpritesInBatch = batch.maxSpritesInBatch;
		statRederCalls = batch.renderCalls;
	}


    private static final float minRatio = 1.5f;

    public void preInitialize(int width, int height)
    {
        screenWidth = width;
        screenHeight = height;
        centerX = screenWidth / 2;
        centerY = screenHeight / 2;

        float ratio = width > height ? (float)width / (float)height : (float) height / (float) width;

        if(ratio <= minRatio)
        {
            allowStatusBar = false;
            useFooter = false;
            useHeader = false;
            modelRatio = minRatio;
        } else
        {
            allowStatusBar = true;
            useFooter = true;
            useHeader = true;
        }
    }

    public void orientationChanged(int newWidth, int newHeight, int angle)
    {
        if(camera != null)
        {
            int newAngle = -angle;

            if(cameraAngle != newAngle)
            {
                camera.rotate(-cameraAngle, 0, 0, 1);
                viewport.update(newAngle != 0 ? screenHeight : screenWidth,
                        newAngle != 0 ? screenWidth : screenHeight, false);
                cameraAngle = newAngle;
                camera.position.set(screenWidth / 2, screenHeight / 2, 0);
                camera.rotate(cameraAngle, 0, 0, 1);
                camera.update();

                IScreenActivator screen = NCore.instance().getScreenFlow().getCurrentScreen();
                if(screen != null)
                {
                    screen.onOrientationChange();
                }
            }
        }
    }

    public void update(int width, int height)
    {
        int newAngle = 0;// -Gdx.input.getRotation();

        camera.rotate(-cameraAngle, 0, 0, 1);
        viewport.update(newAngle != 0 ? screenHeight : screenWidth,
                newAngle != 0 ? screenWidth : screenHeight, false);
        cameraAngle = newAngle;
        camera.position.set(screenWidth / 2, screenHeight / 2, 0);
        camera.rotate(cameraAngle, 0, 0, 1);
        camera.update();
    }

	public void initialize(int width, int height, boolean continuousRender)
	{
        if(height > width)
        {
            screenWidth = width;
            screenHeight = height;
            cameraAngle = 0;
        } else
        {
            screenWidth = height;
            screenHeight = width;
            cameraAngle = 90;
        }

        lastNano = System.nanoTime();
		centerX = screenWidth / 2;
		centerY = screenHeight / 2;

        float ratio = width > height ? (float)width / (float)height : (float) height / (float) width;
        float demul = 1.0f;
        if(ratio <= minRatio)
        {
            useFooter = false;
            useHeader = false;
            demul = ratio / modelRatio;
            modelRatio = minRatio;
        }

		int i;
		for (i = 0; i < gfxWidths.length; i++)
		{
			if (gfxWidths[i] >= screenWidth)
			{
				break;
			}
		}

		if (i >= gfxWidths.length)
		{
			i = gfxWidths.length - 1;
		}

		modelWidth = gfxWidths[i];
		modelGfxName = gfxNames[i];

		float modelHeight = modelWidth * modelRatio;
		factor = (float) screenWidth / (float) modelWidth;


        if(modelHeight*factor > screenHeight) {
            needFactor = true;
            factor = (float)screenHeight / modelHeight * demul;
            modelLen = modelWidth;
            sceneLen = Math.min(screenWidth, (int)(modelWidth * factor));
        } else {
			needFactor = screenWidth != modelWidth;
			modelLen = modelWidth;
			sceneLen = screenWidth;
        }

        gameAreaDeltaX = Math.max((int)((screenWidth - sceneLen)/2), 0);

		gfxDir = ResLoader.singleton().getDataPathPrefix() + modelGfxName + '/';

        Log.i("ncgame", "Chosen resource dir: " + gfxDir);

        batch = new SpriteBatch();

        Log.i("ncgame", "Calculate on resize: m=" + modelWidth + "x" + modelHeight + "/" + modelLen + ", f=" + factor);
        this.continuousRender = continuousRender;
        Gdx.app.getGraphics().setContinuousRendering(continuousRender);
	}

	public String getGfxDir()
	{
		return gfxDir;
	}

	public String getFontDir()
	{
		return "fonts/";
	}

	public int iScale(int orig)
	{
		return needFactor ? orig * sceneLen / modelLen : orig;
	}

	public float fScale(float orig)
	{
		return needFactor ? (int) (orig * factor) : orig;
	}

	public float getScaleFactor()
	{
		return factor;
	}

	protected String lang;

	public String getLang()
	{
		return lang;
	}

    public String getLangCountry()
    {
        return langCountry;
    }

    protected String langCountry;

    public void setLangCountry(String langCountry)
    {
        this.langCountry = langCountry;
    }

	public void setLang(String lang)
	{
		this.lang = lang;
	}

    public static final int LANG_GROUP_US = 0;
    public static final int LANG_GROUP_EU = 1;
    public static final int LANG_GROUP_CYR = 2;

    public int getLangGroup()
    {
        if(lang == null)
            return LANG_GROUP_US;
        if(lang.toLowerCase().contains("ru"))
            return LANG_GROUP_CYR;
        return LANG_GROUP_EU;
    }

	public String getLangFilePath()
	{
		StringBuilder buf = new StringBuilder("lang/");
		if (lang != null && lang.length() >= 2)
		{
			buf.append(lang.substring(0, 2));
			buf.append('/');
		}

		buf.append("strings.xml");

		FileHandle file = Gdx.files.internal(buf.toString());
		if (file.exists())
			return buf.toString();
		return getDefaultLangFilePath();
	}

	public String getDefaultLangFilePath()
	{
		return "lang/strings.xml";		
	}
	
	public String getCommonContentFilePath()
	{
		String localfname = getLayoutDir("common");
		FileHandle file = Gdx.files.internal(localfname);
		if (file.exists())
			return localfname;
		return "layout/common/common.xml";
	}

	public String getLayoutDir(String fname)
	{
		StringBuilder buf = new StringBuilder("layout/");
		buf.append(modelGfxName);
		buf.append('/');
		if (fname != null)
		{
			buf.append(fname);
			buf.append(".xml");
		}
		return buf.toString();
	}

    public String getEffectDir(String fname)
    {
        StringBuilder buf = new StringBuilder("effects/");
        buf.append(modelGfxName);
        buf.append('/');
        if (fname != null)
        {
            buf.append(fname);
            buf.append(".p");
        }
        return buf.toString();
    }

    public String getSoundDir(String soundName)
	{
		StringBuilder buf = new StringBuilder("sound/");
		buf.append(soundName);
		buf.append(".ogg.mp3");
		return buf.toString();
	}

	public String getDeviceModel()
	{
		return "generic";
	}

	public Scaling getImageScaling()
	{
		return Scaling.stretch;
	}

    public Sprite newSpriteShared(String name)
    {
        Sprite sprite = SharedRes.instance.newSprite(name);
        sprite.setSize(fScale(sprite.getWidth()), fScale(sprite.getHeight()));
        return sprite;
    }

    public Sprite newSpriteSharedCenter(String name)
    {
        Sprite sprite = SharedRes.instance.newSprite(name);
        sprite.setSize(fScale(sprite.getWidth()), fScale(sprite.getHeight()));
        sprite.setOrigin(sprite.getWidth()/2f, sprite.getHeight()/2f);
        return sprite;
    }

    public Sprite newSprite(TextureAtlas atlas, String name)
    {
        Sprite sprite = atlas.createSprite(name);
        sprite.setSize(fScale(sprite.getWidth()), fScale(sprite.getHeight()));
        sprite.setOrigin((int)sprite.getOriginX(), (int)sprite.getOriginY());
        return sprite;
    }

    public Sprite newSprite(TextureRegion region)
    {
    	Sprite sprite = new Sprite(region);
        sprite.setSize(fScale(sprite.getWidth()), fScale(sprite.getHeight()));
        sprite.setOriginCenter();
        return sprite;
    }
    
    public void guiSendMessage(Message msg)
    {
        Log.i("ncgame", "Send GUI message with code: " + msg.what);
        synchronized (guiMessages)
        {
            guiMessages.add(msg);
        }
        Gdx.graphics.requestRendering();
        if(!willRefresh())
            resetLastNano();
    }

    private LinkedList<Message> guiMessages = new LinkedList<Message>();

    public long getTimeMillis()
    {
        return System.nanoTime() / 1000000L;
    }


    /**
     * Frame time value in millis
     */
    public long frameStartTime;
    public void processMessagesOnGui()
    {
        frameStartTime = System.nanoTime();
        calcDeltaTime(frameStartTime);
        frameStartTime /= 1000000L;
        synchronized (guiMessages)
        {
            for(Message msg : guiMessages)
            {
                Log.i("ncgame", "Deliver GUI message with code: " + msg.what);
                msg.deliver();
            }
            guiMessages.clear();
        }

        synchronized (postedList)
        {
            for(int i=0;i<postedList.size;i++)
            {
                RunTuple tuple = postedList.get(i);
                if(tuple.startAfter <= frameStartTime)
                {
                    if(tuple.startAfter != 0)
                        subPostRefresh();

                    if(tuple.runnable != null)
                        tuple.runnable.run();
                    postedList.removeValue(tuple, true);
                    i--;
                    backToPool(tuple);
                }
            }
        }
    }

    private Array<RunTuple> postedList = new Array<RunTuple>(false, 32);

    private static class RunTuple
    {
        Runnable runnable;
        long     startAfter;

        RunTuple nextFree;

        RunTuple()
        {

        }
        RunTuple init(Runnable runnable)
        {
            this.runnable = runnable;
            startAfter = 0;
            return this;
        }

        RunTuple init(Runnable runnable, long startAfter)
        {
            this.runnable = runnable;
            this.startAfter = startAfter;
            return this;
        }
    }

    private RunTuple freeTuple = null;

    private RunTuple obtainTuple()
    {
        if(freeTuple != null)
        {
            synchronized (this)
            {
                if(freeTuple != null)
                {
                    RunTuple msg = freeTuple;
                    freeTuple = msg.nextFree;
                    msg.nextFree = null;
                    return msg;
                }
            }

        }
        return new RunTuple();
    }

    private synchronized void backToPool(RunTuple tuple)
    {
        tuple.runnable = null;
        tuple.nextFree = freeTuple;
        freeTuple = tuple;
    }

    /**
     * Post runnable to be run on GUI thread on next frame
     * @param run
     */
    public void post(Runnable run)
    {
        RunTuple tuple = obtainTuple();
        tuple.init(run);
        synchronized (postedList)
        {
            postedList.add(tuple);
        }
        addTempRefresh();
        doRefresh();
    }

    /**
     * Post runnable to be run on GUI thread after given delay in millis
     * @param run
     * @param delayMillis
     */
    public void postDelayed(Runnable run, long delayMillis)
    {
        RunTuple tuple = obtainTuple();
        tuple.init(run, delayMillis + frameStartTime);
        synchronized (postedList)
        {
            postedList.add(tuple);
        }
        addPostRefresh();
        doRefresh();
    }

    /**
     * Remove runnable from the waiting queue if runnable was posted via post or postDelayed
     * @param toRemove
     */
    public void removePosted(Runnable toRemove)
    {
        synchronized (postedList)
        {
            for(int i = 0; i<postedList.size;i++)
            {
                RunTuple tuple = postedList.get(i);
                if(tuple.runnable == toRemove)
                {
                    if(tuple.startAfter != 0)
                        subPostRefresh();
                    backToPool(postedList.removeIndex(i));
                    i--;
                }
            }
        }
    }

    private int postCounter = 0;

    private void addPostRefresh()
    {
        postCounter++;
    }

    private void subPostRefresh()
    {
        postCounter--;
        if(postCounter < 0)
        {
            postCounter = 0;
        }
    }


    public int getRefreshCounter()
    {
        return refreshCounter;
    }

    public int getTempRefresh()
    {
        return tempRefresh;
    }

    private int refreshCounter = 0; //if counter > 0 then redraw every frame at 60 fps, otherwise do not redraw
    private int tempRefresh = 1;

    private static final int AFTER_REFRESHES = 1;
    public void addRefresh()
    {
        refreshCounter ++;
    }

    public void subRefresh()
    {
        refreshCounter --;
        if(refreshCounter <= 0)
        {
            refreshCounter = 0;
            tempRefresh += AFTER_REFRESHES;
        }
    }

    public void doRefresh()
    {
        if(willRefresh())
        {
            if(!continuousRender)
                Gdx.app.getGraphics().requestRendering();

            tempRefresh =  tempRefresh > 0 ?  tempRefresh - 1 : 0;
        }
    }

    public boolean willRefresh()
    {
        return continuousRender || refreshCounter > 0 || tempRefresh > 0 || postCounter > 0;
    }

    public void resetRefresh()
    {
        refreshCounter = 0;
        tempRefresh += AFTER_REFRESHES;
    }

    public void addTempRefresh()
    {
        if(tempRefresh > 40)
            return;
        tempRefresh ++;
    }

    private WindowedMean mean = new WindowedMean(5);
    private float rawDelta = 0;
    private long lastNano;

    public void updateMean(float delta)
    {
        rawDelta = delta;
        mean.addValue(delta);
    }

    public float getDeltaTime()
    {
        return mean.hasEnoughData() ? mean.getMean() : rawDelta;
    }

    public float getRawDelta()
    {
        return rawDelta;
    }

    private float calcDeltaTime(long curNano)
    {
        rawDelta = (curNano - lastNano) / 1000000000.0f;
        updateMean(rawDelta);
        lastNano = curNano;
        return rawDelta;
    }

    public void resetLastNano()
    {
        lastNano = System.nanoTime();
        mean.clear();
    }

    public long getLastNano()
    {
        return lastNano;
    }

    public boolean isPaidVersion(int version)
    {
        return (version >= 200 && version < 300) || version >= 2000;
    }

    public boolean needHeader()
    {
        return false; //useHeader;
    }

    public boolean needFooter()
    {
        return false; //useFooter;
    }

    public boolean isAllowStatusBar()
    {
        return allowStatusBar;
    }

    public boolean silhouetteOn;

    public Sprite cloneSprite(Sprite sprite)
    {
        if(sprite instanceof TextureAtlas.AtlasSprite)
        {
            return new TextureAtlas.AtlasSprite((TextureAtlas.AtlasSprite)sprite);
        }

        return new Sprite(sprite);
    }
}
