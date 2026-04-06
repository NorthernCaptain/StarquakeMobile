package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.tools.loaders.XMLContentLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.Point;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.core.Quad;
import northern.captain.quadronia.game.events.EGameLevelUp;
import northern.captain.quadronia.game.events.EGameOver;
import northern.captain.quadronia.game.events.EGameStart;
import northern.captain.quadronia.game.events.EScoreChange;
import northern.captain.quadronia.gfx.GameOverDraw;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IAction;
import northern.captain.quadronia.gfx.ICursorEventListener;
import northern.captain.quadronia.gfx.IDraw;
import northern.captain.quadronia.gfx.IGraphicsInit;
import northern.captain.quadronia.gfx.TextMoveActor;
import northern.captain.quadronia.gfx.elements.QuadsDraw;
import northern.captain.quadronia.gfx.panels.PanelPerks;
import northern.captain.quadronia.gfx.panels.PanelScoreOnly;
import northern.captain.quadronia.gfx.panels.PanelTimeOnly;
import northern.captain.tools.IDisposable;

/**
 * Created by leo on 01.09.15.
 */
public class GameFieldWidget extends Widget implements IGraphicsInit, IDisposable, ICursorEventListener
{
    private int width;
    private int height;

    private static final int MAX_COLORS = 12;

    private QuadsDraw quadsDraw = new QuadsDraw();
    private Game game;

    private TextMoveActor floatingScoreText;

    private Point point = new Point();

    private PanelTimeOnly panelTimeOnly;
    private PanelScoreOnly panelScoreOnly;
    private PanelPerks panelPerks;

    private XMLLayoutLoader loader;
    private GraphicsInitContext graphicsInitContext;

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {


        this.loader = loader;
        this.graphicsInitContext = gContext;

        XMLContentLoader.Node node = loader.getNode("fld");

        quadsDraw.initGraphics(loader, gContext);
        floatingScoreText = new TextMoveActor("d40b", 0, NContext.current.screenHeight/3, 0.8f);

        NCore.busRegister(this);
    }

    public void setGame(Game game)
    {
        this.game = game;
        quadsDraw.setGame(game);
        graphicsInitContext.deltaY = -NContext.current.gameAreaDeltaY;
        graphicsInitContext.deltaX = -NContext.current.gameAreaDeltaX;
        panelTimeOnly = new PanelTimeOnly(game, this);
        panelTimeOnly.initGraphics(loader, graphicsInitContext);
        panelScoreOnly = new PanelScoreOnly(game);
        panelScoreOnly.initGraphics(loader, graphicsInitContext);
        panelPerks = new PanelPerks(game);
        panelPerks.initGraphics(loader, graphicsInitContext);
        graphicsInitContext.deltaX = 0;
    }

    public PanelTimeOnly getPanelTimeOnly()
    {
        return panelTimeOnly;
    }

    @Override
    public float getMinWidth () {
        return width;
    }

    @Override
    public float getMinHeight () {
        return height;
    }

    @Override
    public float getPrefWidth () {
        return width;
    }

    @Override
    public float getPrefHeight () {
        return height;
    }

    @Override
    public float getMaxWidth () {
        return width;
    }

    @Override
    public float getMaxHeight () {
        return height;
    }

    private Array<IDraw> drawables = new Array<IDraw>(false, 32);
    private Array<ICursorEventListener> touchables = new Array<>(false, 16);

    public void addIDraw(IDraw draw)
    {
        drawables.add(draw);
        draw.parentWidget(this);
    }

    public void addITouchable(ICursorEventListener item) {
        touchables.add(item);
    }

    public IDraw findDrawByLogicObject(Object obj)
    {
        for(IDraw draw : drawables)
        {
            if(draw.hasLogicObject(obj))
                return draw;
        }

        return null;
    }

    public void removeDraw(IDraw draw)
    {
        drawables.removeValue(draw, true);
    }

    public void removeTouchable(ICursorEventListener item) {touchables.removeValue(item, true);}

    private Array<IAction> actions = new Array<IAction>(false, 16);

    public void addIAction(IAction action)
    {
        actions.add(action);
    }

    public void removeAction(IAction action)
    {
        actions.removeValue(action, true);
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);
        quadsDraw.act(delta);
        panelTimeOnly.act(delta);
        panelScoreOnly.act(delta);
        panelPerks.act(delta);
        for(IAction action : actions)
        {
            action.act(delta);
        }
    }

    /**
     * If this method is overridden, the super method or {@link #validate()} should be called to ensure the widget is laid out.
     *
     * @param batch
     * @param parentAlpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);

        panelTimeOnly.draw(batch, parentAlpha);
        panelScoreOnly.draw(batch, parentAlpha);

        quadsDraw.drawBG(batch, parentAlpha);

        panelPerks.draw(batch, parentAlpha);

        quadsDraw.draw(batch, parentAlpha);

        for(IDraw draw : drawables)
        {
            draw.draw(batch, parentAlpha);
        }
    }

    @Override
    public void dispose()
    {
        quadsDraw.dispose();
        panelTimeOnly.dispose();
        panelPerks.dispose();
        drawables.clear();
        actions.clear();
        NCore.busUnregister(this);
    }

    private Quad touchedQ;

    /**
     * Process touch down event if it's yours
     *
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        for(ICursorEventListener item: touchables) {
            if(item.doTouchDown(fx, fy)) return true;
        }

        if(panelPerks.doTouchDown(fx, fy)) return true;

        touchedQ = game.getField().getQuadByCoord(fx, fy);
        return touchedQ != null || panelTimeOnly.doTouchDown(fx, fy);
    }

    /**
     * Process drag event
     *
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    @Override
    public boolean doDrag(int fx, int fy)
    {
        return false;
    }

    /**
     * Process 'release' touch up event
     *
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        for(ICursorEventListener item: touchables) {
            if(item.doTouchUp(fx, fy)) return true;
        }

        Quad upQ = game.getField().getQuadByCoord(fx, fy);
        if(upQ != null && upQ == touchedQ)
        {
            game.selectQuad(touchedQ);
            return true;
        }

        return panelTimeOnly.doTouchUp(fx, fy);
    }

    @Override
    public Vector2 localToStageCoordinates(Vector2 localCoords)
    {
        localCoords.x += getX();
        localCoords.y += getY();
        return localCoords;
    }

    @Subscribe
    public void onScoreChangeFloating(EScoreChange event)
    {
        floatingScoreText.hide();
        if(event.delta > 0)
        {
            game.getField().getQuadCenterByXY(event.centerCX, event.centerCY, point);
            floatingScoreText.init(point.x, point.y, "+" + event.delta);
        } else
        {
            point.x = NContext.current.centerX;
            point.x = point.x + point.x / 2;
            point.y = NContext.current.centerY;
            point.y = point.y + point.y / 2;
            floatingScoreText.init(point.x, point.y, "" + event.delta, 1f, 0.2f, 0.3f);
        }
        floatingScoreText.show();

        panelScoreOnly.onScoreChange(event);
    }

    @Subscribe
    public void onGameOver(EGameOver event)
    {
        GameOverDraw draw = new GameOverDraw();
        draw.initGraphics(loader, graphicsInitContext.atlas);
        addIDraw(draw);
        addIAction(draw);
        game.getContext().setDatai(IGameContext.LAST_SCREEN, IScreenWorkflow.STATE_MAIN_MENU);
    }

    @Subscribe
    public void onGameLevelUp(EGameLevelUp event)
    {
        panelScoreOnly.onLevelChanged(event);
    }

    @Subscribe
    public void onGameStart(EGameStart event)
    {
        setGame(event.game);
        panelScoreOnly.onScoreChange(new EScoreChange(event.game.getScore(), 0, 0, 0));
        try
        {
            if(event.restored && game.savedGame != null)
            {
                panelTimeOnly.deserializeJSON(game.savedGame.getJSONObject("tim"));
            }
        } catch (JSONException jex) {}
    }
}
