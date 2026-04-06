package northern.captain.gamecore.glx.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.core.Game;
import northern.captain.tools.Log;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

public class SplashScreen extends ScreenBase
{

	private LoadIds load = new LoadIds();
	
	private TextureAtlas tatlas;
	
	private Image splashImage;
	
	public SplashScreen(IScreenWorkflow sflow)
	{
		super(sflow);
	}

	private boolean isLoading = false;
	
	@Override
	public void show()
	{
		super.show();

		load.layoutId = ResLoader.singleton().loadLayout("splash");
		
		ResLoader.singleton().finishLoading();
		SharedRes.instance.endLoadingPart1();
		
        tatlas = SharedRes.instance.getCommonAtlas();

		NCore.instance().getScreenFlow().setCurrentState(IScreenWorkflow.STATE_INITIAL);
	}
	
	@Override
	public void resize(
			int width,
			int height )
	{
		super.resize( width, height );

		// let's make sure the stage is clear
		stage.clear();
		
		XMLLayoutLoader layXml = ResLoader.singleton().getLoaded(load.layoutId);

        screenTable = mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.setName("mainT");

        Stack frame = new Stack();

        mainTable.add(frame).expand().fill();

        Table tbl = new Table();
		splashImage = layXml.newImage("intro", tatlas);
		splashImage.getColor().a = 0f;
		layXml.addToCell(tbl, splashImage);

        Image bg = layXml.newImage("back", tatlas);

        bg.setSize(NContext.current.screenWidth, NContext.current.screenHeight);
        frame.add(bg);
        frame.add(tbl);

        splashImage.addAction(Animations.seq(delay(0.2f), fadeIn(2.0f), delay(0.2f), Actions.run(new Runnable()
        {
            @Override
            public void run()
            {
                SharedRes.instance.startLoadingPart2();
                NCore.instance().loadOnIntro();
                isLoading = true;
            }
        })));

        stage.addActor( mainTable );
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.gamecore.glx.screens.ScreenBase#render(float)
	 */
	@Override
	public void render(float delta)
	{
		super.render(delta);
		if(isLoading)
		{
			if(ResLoader.singleton().update())
			{
				isLoading = false;
				splashDone();
			}
		} else
			ResLoader.singleton().update();
	}
	
	public void splashDone()
	{
		SharedRes.instance.endLoadingPart2();
//        try
//        {
//            Thread.sleep(10000);
//        }
//        catch(Exception ex) {}
		Log.d("ncae", "Splash - done loading main resources");
        IScreenWorkflow flow = NCore.instance().getScreenFlow();
        int action = IScreenWorkflow.WF_STAY;

		if(Game.defaultGameContext.getDatai(IGameContext.LAST_SCREEN) == IScreenWorkflow.STATE_BATTLE)
			action = IScreenWorkflow.WF_NEW_GAME;

		flow.setCurrentState(IScreenWorkflow.STATE_MAIN_MENU);

		try
        {
            flow.prepare(action);
            nextScreen(action);
        } catch(Exception ex)
        {
            Log.e("ncgame", "ERROR on START while switching to first screen " + ex);
            flow.setCurrentState(IScreenWorkflow.STATE_MAIN_MENU);
            flow.prepare(IScreenWorkflow.WF_STAY);
            nextScreen(IScreenWorkflow.WF_STAY);
        }

//		screenFadeOut(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				nextScreen(action);
//			}
//		});
	}

	public void nextScreen(int action)
	{
		Log.d("ncae", "Splash - go to next screen");
        IScreenWorkflow flow = NCore.instance().getScreenFlow();
        flow.doAction(action);
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.gamecore.glx.screens.ScreenBase#hide()
	 */
	@Override
	public void hide()
	{
		super.hide();
	}

	@Override
	public void dispose()
	{		
		if(splashImage != null)
		{
			splashImage = null;
			super.dispose();
//			ResLoader.singleton().unload(load.tatlasId);
			ResLoader.singleton().unload(load.layoutId);
		}
	}

	@Override
	public void activate(ScreenContext sctx)
	{
	}

}
