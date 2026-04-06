package northern.captain.quadronia.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenBase;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.widget.TripleButton;

public class ScreenShopStandalone extends ScreenBase
{
    private LoadIds load = new LoadIds();
    private TextureAtlas atlas;
    private XMLLayoutLoader layXml;
    private SubScreenShop shop;

    public ScreenShopStandalone(IScreenWorkflow workflow)
    {
        super(workflow);
    }

    @Override
    public void show()
    {
        super.show();
        ResLoader.singleton().finishLoading();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        stage.clear();
        Group maingrp = new Group();

        atlas = ResLoader.singleton().getLoaded(load.tatlasId);
        layXml = ResLoader.singleton().getLoaded(load.layoutId);

        initScreenBack(layXml, atlas);

        Stack frame = new Stack();
        frame.setName("gframe");
        NContext.current.setGameFrame(frame);

        GraphicsInitContext gContext = new GraphicsInitContext(atlas);

        shop = new SubScreenShop();
        shop.initGraphics(layXml, gContext);
        maingrp.addActor(shop);

        TripleButton backBut = new TripleButton("shop_back");
        backBut.initGraphics(layXml, gContext);
        backBut.setOnClick(()->{
            flow.prepare(IScreenWorkflow.WF_BACK);
            NContext.current.post(()->flow.doAction(IScreenWorkflow.WF_BACK));
        });
        maingrp.addActor(backBut);
        maingrp.setX(NContext.current.gameAreaDeltaX);
        stage.addActor(maingrp);

        NCore.busRegister(this);
    }
    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.screens.ScreenBase#dispose()
     */
    @Override
    public void dispose()
    {
        super.dispose();
        ResLoader.singleton().unload(load.tatlasId);
        ResLoader.singleton().unload(load.layoutId);
        NCore.busUnregister(this);
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.screens.ScreenBase#prepareEnter()
     */
    @Override
    public void prepareEnter()
    {
        super.prepareEnter();
        load.tatlasId = ResLoader.singleton().loadTextureAtlas("virtual-atlas/atlas");
        load.layoutId = ResLoader.singleton().loadLayout("vscreens");
    }
}
