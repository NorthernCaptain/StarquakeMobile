package northern.captain.gamecore.glx.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import northern.captain.quadronia.TheGame;
import northern.captain.quadronia.gfx.ICursorEventListener;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.TiledDrawable2;
import northern.captain.gamecore.glx.tools.Vector2i;
import northern.captain.gamecore.glx.tools.loaders.XMLContentLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

public abstract class ScreenBase implements Screen, IScreenActivator, ICursorEventListener
{
	protected static class LoadIds
	{
        public LoadIds() {}

        public int tatlasId;
		public int layoutId;
	}

    protected IScreenWorkflow flow;

	protected Stage	stage;

    protected TextureRegion boardTile;
    protected Rectangle boardRect;
    protected Rectangle scissorsBoardRect;

    protected Color clearColor = Color.BLACK;

	protected Table screenTable, mainTable, gameAreaTable;	
	
	public ScreenBase(IScreenWorkflow screenFlow)
	{
		flow = screenFlow;
	}

    protected void renderBG(Batch batch)
	{
        batch.begin();
   		ScissorStack.calculateScissors(stage.getCamera(), batch.getTransformMatrix(),
   				boardRect, scissorsBoardRect);
        ScissorStack.pushScissors(scissorsBoardRect);

        int regWidth = boardTile.getRegionWidth();
        int regHeight = boardTile.getRegionHeight();
        
        int tilesNX = ((int)boardRect.width + regWidth - 1) / regWidth;
        int tilesNY = ((int)boardRect.height + regHeight - 1) / regHeight;

        for(int y=0;y<tilesNY;y++)
        {
        	float dy = boardRect.y + y * regHeight;
        	for(int x = 0;x<tilesNX;x++)
        	{
        		batch.draw(boardTile, boardRect.x + x * regWidth, dy);
        	}
        }
        
        batch.end();
        ScissorStack.popScissors();
	}
	
	@Override
	public void render(float delta)
	{
		// the following code clears the screen with the given RGB color (black)
        Gdx.gl.glClearColor( clearColor.r, clearColor.g, clearColor.b, clearColor.a );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

        if(stage != null)
        {
            if(delta > 0.2)
            {
                //Assume that we are back from sleep in non-rendering mode
                delta = TheGame.standardFrameDuration;
                NContext.current.resetLastNano();
                NContext.current.addTempRefresh();
            }
            stage.act( delta );
            stage.draw();
        }
        NContext.current.updateStats(NContext.current.batch);
		Thread.yield();
	}

    protected static Viewport vp = new ScreenViewport();

	@Override
	public void resize(int width, int height)
	{
        if(stage == null)
            show();

        NContext.current.update(width, height);
		stage.setViewport(vp);
        NContext.current.resetLastNano();
	}

    private boolean skipAct = false;

    private InputProcessor inputProcessor = new InputProcessor()
    {
        @Override
        public boolean keyDown(int keycode)
        {
            if(!NContext.current.willRefresh())
                NContext.current.resetLastNano();

            if(keycode == Input.Keys.BACK)
            {
                NCore.instance().onBackPressed();
            }

            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean keyUp(int keycode)
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean keyTyped(char character)
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button)
        {
            if(!NContext.current.willRefresh())
                NContext.current.resetLastNano();

            return doTouchDown(screenX, screenY);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button)
        {
            return doTouchUp(screenX, screenY);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer)
        {
            return doDrag(screenX, screenY);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY)
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }
    };

    protected Vector2i vector2i = new Vector2i();

    public Vector2i screenToWorld(int x, int y)
    {
        if(NContext.current.cameraAngle == 0)
        {
            vector2i.x = x;
            vector2i.y = NContext.current.screenHeight - y;
        } else
        {
            vector2i.y = NContext.current.screenHeight - x;
            vector2i.x = NContext.current.screenWidth - y;
        }

        return vector2i;
    }

	@Override
	public void show()
	{
		stage = new Stage(vp, NContext.current.batch);
		Gdx.input.setInputProcessor(new InputMultiplexer(inputProcessor, stage));
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
		NContext.current.setCurrentStage(stage);
	}

	@Override
	public void hide()
	{
		NContext.current.unsetCurrentStage(stage);
		postDispose();
	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub

	}

	private Runnable disposeRun = ()->dispose();
	
	public void postDispose()
	{
		Gdx.app.postRunnable(disposeRun);
	}
	
	@Override
	public void dispose()
	{
		stage.dispose();
		stage = null;
		screenTable = mainTable = gameAreaTable = null;
	}

    private float logoAlpha = 0.3f;

    public void setLogoAlpha(float logoAlpha)
    {
        this.logoAlpha = logoAlpha;
    }

    public float deltaGameAreaY = 0;

    /**
	 * Initializes back cover of the screen. Next specific screens should add their content into the gameAreaTable.
	 * Other table areas inside screenTable are subject to change on the fly, do not use them
	 * @param layXml
	 * @param tatlas
	 */
	protected void initScreenBack(XMLLayoutLoader layXml, TextureAtlas tatlas)
	{
		XMLContentLoader.Node screenNode = layXml.getNode("screen");

        initBackgroundImage(layXml, tatlas, screenNode);

		mainTable = new Table();
		mainTable.setFillParent(true);
		mainTable.setName("mainT");


		screenTable = new Table();
		screenTable.setName("screenT");

		mainTable.add(screenTable);

        if(NContext.current.needHeader())
        {
            Image header = layXml.newImage("header", tatlas);
            layXml.addToCell(screenTable, header);
            screenTable.row();
        }


        gameAreaTable = new Table();
		gameAreaTable.setName("gameT");
		float w = NContext.current.fScale(layXml.getAttribFloat(screenNode, "w", 480));
		float h = NContext.current.fScale(layXml.getAttribFloat(screenNode, "h", 720));

        if(h > NContext.current.screenHeight)
        {
            h = NContext.current.screenHeight;
        }

        if(h > 1080 && NContext.current.screenHeight > h)
        {
            h = NContext.current.screenHeight;
        }

        NContext.current.gameAreaHeight = (int)h;
        NContext.current.gameAreaDeltaY = (NContext.current.screenHeight - NContext.current.gameAreaHeight)/2;
        deltaGameAreaY = (NContext.current.screenHeight - h)/2;

        gameAreaTable.setSize(w, h);
        screenTable.add(gameAreaTable)
        			.width(w)
					.height(h);

        screenTable.row();

        if(NContext.current.needFooter())
        {
            layXml.addToCell(screenTable, layXml.newImage("footer", tatlas));
        }

//		gameAreaTable.setClip(true);
        String bgFName = layXml.getAttribString(screenNode, "bg", null);
        if(bgFName != null)
        {
            boardTile = tatlas.findRegion(bgFName);
            String bgtype = layXml.getAttribString(screenNode, "bgtype", "tile");

            if(bgtype.equals("tile"))
            {
                gameAreaTable.setBackground(new TiledDrawable2(boardTile)
                    .setScaleFactor(layXml.getAttribFloat(screenNode, "scale", NContext.current.getScaleFactor()))
                    .setDelta(
                        layXml.getAttribFloat(screenNode, "dx", 0),
                        layXml.getAttribFloat(screenNode, "dy", 0)));
            } else
                if(bgtype.equals("nine"))
                {
                    NinePatch ninePatch = tatlas.createPatch(bgFName);
                    NinePatchDrawable drawable = new NinePatchDrawable(ninePatch);
                    drawable.setMinWidth(NContext.current.fScale(layXml.getAttribFloat(screenNode, "dw", drawable.getMinWidth())));
                    drawable.setMinHeight(NContext.current.fScale(layXml.getAttribFloat(screenNode, "dh", drawable.getMinHeight())));
                    gameAreaTable.setBackground(drawable);
                } else
                {
                    gameAreaTable.setBackground(new TextureRegionDrawable(boardTile));
                }

        }

        Integer adHeight = SharedRes.instance.getCommon().getNodeValueInteger("adh");
        if(adHeight != null && adHeight > 0)
        {
            Table adSpacer = new Table();
            mainTable.row();
            mainTable.add(adSpacer).expandX().height(adHeight);
        }

		stage.addActor( mainTable );
	}

    protected void initBackgroundImage(XMLLayoutLoader layXml, TextureAtlas tatlas, XMLContentLoader.Node screenNode)
    {
        String fillName = layXml.getAttribString(screenNode, "fill", "bg");

        if(!"none".equals(fillName))
        {
            TiledDrawable2 bgFill =
                    new TiledDrawable2(tatlas.findRegion(fillName))
                            .setScaleFactor(layXml.getAttribFloat(screenNode, "scale", NContext.current.getScaleFactor()));

            Image background = new Image(bgFill);
            background.setFillParent(true);
            stage.addActor(background);

            Image intro = layXml.newImage("logo", tatlas);
            if(intro != null)
            {
                intro.setColor(1,1,1,logoAlpha);
                Table introTable = new Table();
                introTable.add(intro).width(intro.getWidth()).height(intro.getHeight());
                introTable.setFillParent(true);
                stage.addActor(introTable);
            }
        }
    }

	
	@Override
	public void deactivate(ScreenContext sctx)
	{
        NContext.current.resetRefresh();
	}

	@Override
	public boolean onBackAction()
	{
		return true;
	}

	@Override
	public void onFullSuspend()
	{
	}

	@Override
	public void onFullResume()
	{
	}

	@Override
	public void prepareEnter()
	{
	}

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenActivator#activate(northern.captain.seabattle.screens.ScreenContext)
	 */
	@Override
	public void activate(ScreenContext sctx)
	{
		TheGame.instance.setScreen(this);
	}
	
	public void screenFadeOut(Runnable runAfter)
	{
        if(screenTable != null)
        {
		    stage.addAction(Animations.getScreenFadeOut(runAfter));
            NContext.current.addRefresh();
        }
        else
        {
            NContext.current.post(runAfter);
        }
	}

    public void screenFadeOut(Runnable runAfter, float delayBefore)
    {
        if(screenTable != null)
        {
            screenTable.addAction(Animations.getScreenFadeOut(delayBefore, runAfter));
            NContext.current.addRefresh();
        }
        else
        {
            NContext.current.post(runAfter);
        }
    }

	public void screenFadeIn()
	{
		screenTable.getColor().a = 0;
		screenTable.addAction(Animations.getScreenFadeIn());
	}

    public void openArmory()
    {
    }

    public void closeArmory()
    {
    }

    @Override
    public void onOrientationChange()
    {

    }

    @Override
    public boolean doDrag(int fx, int fy)
    {
        return false;
    }

    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        return false;
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        return false;
    }
}
