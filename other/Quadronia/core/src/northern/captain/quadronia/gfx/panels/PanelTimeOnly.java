package northern.captain.quadronia.gfx.panels;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.IGameTimer;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.EGameResumeNow;
import northern.captain.quadronia.game.events.EGameTimeOver;
import northern.captain.quadronia.game.events.ENewTimePeriod;
import northern.captain.quadronia.game.events.EOpenShop;
import northern.captain.quadronia.game.events.EQuadAreaHitStart;
import northern.captain.quadronia.game.events.ETimeDeltaChange;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.ICursorEventListener;
import northern.captain.quadronia.gfx.ISimpleDrawable;
import northern.captain.quadronia.gfx.PauseDraw;
import northern.captain.quadronia.gfx.RawButton;
import northern.captain.quadronia.gfx.widget.GameFieldWidget;
import northern.captain.quadronia.gfx.widget.TripleButton;
import northern.captain.tools.IDisposable;
import northern.captain.tools.IJSONSerializer;

/**
 * Created by leo on 19.04.15.
 */
public class PanelTimeOnly implements ISimpleDrawable, ICursorEventListener, IGameTimer, IDisposable, IJSONSerializer
{
    private boolean onPause = false;

    public Label timeValLabel;
    private Image timerBack;
    private Image timerKnob;
    private Image currentKnob;
    private Image timerKnob2;
    private float rightX, leftX;
    private TripleButton coinsBut;


    private int totalSec = 30;
    private int spentSec = -1;
    private float spentFSec= 0;

    private Game game;
    private GameFieldWidget fieldWidget;

    private RawButton timeButton;
    private RawButton coinsClick;

    private PauseDraw pauseDraw = new PauseDraw();

    public PanelTimeOnly(Game game, GameFieldWidget fieldWidget)
    {
        this.game = game;
        this.fieldWidget = fieldWidget;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        timerBack.draw(batch, parentAlpha);
        timeValLabel.draw(batch, parentAlpha);
        currentKnob.draw(batch, parentAlpha);
        coinsBut.draw(batch, parentAlpha);
    }

    @Override
    public void drawFBO(Batch fboBatch, float parentAlpha)
    {
    }

    @Override
    public void initGraphics(final XMLLayoutLoader layXml, GraphicsInitContext gContext)
    {
        final TextureAtlas atlas = gContext.atlas;
        timerBack = layXml.newImage("timerback", gContext.atlas);
        timerKnob = layXml.newImage("timerknob", gContext.atlas);
        timerKnob2 = layXml.newImage("timerknob2", gContext.atlas);

        coinsBut = new TripleButton("h_coins");
        coinsBut.initGraphics(layXml, gContext);

        currentKnob = timerKnob2;

        timeValLabel = layXml.newLabel("timeval");

        if (gContext.deltaY != 0 || gContext.deltaX != 0)
        {
            timerBack.setPosition(timerBack.getX() - gContext.deltaX, timerBack.getY() + gContext.deltaY);
            timerKnob.setPosition(timerKnob.getX() - gContext.deltaX, timerKnob.getY() + gContext.deltaY);
            timerKnob2.setPosition(timerKnob2.getX() - gContext.deltaX, timerKnob2.getY() + gContext.deltaY);
            timeValLabel.setPosition(timeValLabel.getX() - gContext.deltaX, timeValLabel.getY() + gContext.deltaY);
            coinsBut.setPosition(coinsBut.getX() - gContext.deltaX, coinsBut.getY() + gContext.deltaY);
        }


        leftX = timerKnob.getX();
        rightX = leftX + timerBack.getWidth() - timerKnob.getWidth();

        timeButton = new RawButton((int) timerBack.getX(), (int) timerBack.getY(),
            (int) (timerBack.getX() + timerBack.getWidth()),
            (int) (timerBack.getY() + timerBack.getHeight()))
        {
            @Override
            public boolean doTouchUp(int fx, int fy)
            {
                if (super.doTouchUp(fx, fy))
                {
                    if (game.isPlaying)
                    {
                        game.doPause();
                        pauseTimer();
                        PauseDraw draw = pauseDraw = new PauseDraw();
                        draw.initGraphics(layXml, atlas);
                        fieldWidget.addIDraw(draw);
                        fieldWidget.addIAction(draw);
                        fieldWidget.addITouchable(draw);
                        currentKnob.setX(leftX);
                        currentKnob.addAction(Actions.moveTo(rightX, timerKnob.getY(), 0.3f, Interpolation.swingOut));
                        NCore.instance().getSoundman().playSound(ISoundMan.SND_PAUSED, true);
                    } else
                    {
                        onGameResume(null);
                    }
                    return true;
                }
                return false;
            }
        };

        coinsClick = new RawButton((int) coinsBut.getX(), (int) coinsBut.getY(),
                (int) (coinsBut.getX() + coinsBut.getWidth()),
                (int) (coinsBut.getY() + coinsBut.getHeight()))
        {
            @Override
            public boolean doTouchUp(int fx, int fy)
            {
                if (super.doTouchUp(fx, fy))
                {
                    NCore.busPost(new EOpenShop());
                    return true;
                }
                return false;
            }
        };

        coinsBut.setText(Integer.toString(UserManager.instance.getCurrentUser().getCoins()));
        NCore.busRegister(this);
    }

    @Override
    public void setOnTimedOut(Runnable callback)
    {

    }

    @Override
    public void resetTimer(int totalSec)
    {
        this.totalSec = totalSec;
        spentFSec = 0;
        spentSec = 0;
        timeValLabel.setText(String.format("%02d:%02d", totalSec / 60, totalSec % 60));
        coinsBut.setText(Integer.toString(UserManager.instance.getCurrentUser().getCoins()));
        onPause = !game.isPlaying;
        if(timerKnob != null)
        {
            if(onPause)
            {
                currentKnob = timerKnob;
                currentKnob.setX(rightX);
            } else
            {
                currentKnob = timerKnob2;
                currentKnob.setX(leftX);
            }
        }
        NCore.instance().getSoundman().stopSound(ISoundMan.SND_TIME_ALMOST_OVER);
    }

    @Override
    public void pauseTimer()
    {
        if(timerKnob != null)
        {
            currentKnob = timerKnob;
            currentKnob.setX(rightX);
        }
        onPause = true;
        NCore.instance().getSoundman().stopSound(ISoundMan.SND_TIME_ALMOST_OVER);
    }

    @Override
    public void resumeTimer()
    {
        if(timerKnob2 != null)
        {
            currentKnob = timerKnob2;
            currentKnob.setX(leftX);
        }
        onPause = !game.isPlaying;
    }

    public void act(float delta)
    {
        currentKnob.act(delta);
        coinsBut.act(delta);

        if(onPause) return;

        spentFSec += delta;

        int sec = (int)spentFSec;
        if(sec > totalSec) sec = totalSec;

        int rest = totalSec - sec;
        if(sec != spentSec)
        {
            spentSec = sec;
            timeValLabel.setText(String.format("%02d:%02d", rest / 60, rest % 60));
            coinsBut.setText(Integer.toString(UserManager.instance.getCurrentUser().getCoins()));
            if(rest == 5)
            {
                NCore.instance().getSoundman().playSound(ISoundMan.SND_TIME_ALMOST_OVER, true);
            }
        }

        if(spentFSec >= totalSec)
        {
            pauseTimer();
            NCore.busPost(new EGameTimeOver(this));
        }
    }

    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        if(timeButton.doTouchDown(fx, fy)) return true;
        if(coinsClick.doTouchDown(fx, fy)) return true;
        return false;
    }

    @Override
    public boolean doDrag(int fx, int fy)
    {
        return false;
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        if(timeButton.doTouchUp(fx, fy)) return true;
        if(coinsClick.doTouchUp(fx, fy)) return true;
        return false;
    }

    @Subscribe
    public void onNewTimePeriod(ENewTimePeriod event)
    {
        if(event.periodDurationSec == 0)
        {
            resumeTimer();
        } else
        {
            resetTimer(event.periodDurationSec);
        }
    }

    @Subscribe
    public void onTimeDeltaChange(ETimeDeltaChange event)
    {
        this.totalSec += event.deltaTimeSec;
        act(0);
    }

    @Subscribe
    public void onQuadAreaHit(EQuadAreaHitStart event)
    {
        onPause = true;
        NCore.instance().getSoundman().stopSound(ISoundMan.SND_TIME_ALMOST_OVER);
    }

    @Subscribe
    public void onGameResume(EGameResumeNow event)
    {
        if(game.isPlaying) return;
        game.doResume();
        resumeTimer();
        if (pauseDraw != null)
        {
            fieldWidget.removeAction(pauseDraw);
            fieldWidget.removeDraw(pauseDraw);
            fieldWidget.removeTouchable(pauseDraw);
            pauseDraw = null;
        }
        currentKnob.setX(rightX);
        currentKnob.addAction(Actions.moveTo(leftX, timerKnob.getY(), 0.3f, Interpolation.swingOut));
    }

    @Override
    public void dispose()
    {
        NCore.busUnregister(this);
    }

    /**
     * Serialize object into the JSONObject. Object should create a new JSONObject,
     * put all data into it and then return this json to the caller.
     */
    @Override
    public JSONObject serializeJSON()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("s", (double)spentFSec);
            json.put("ts", totalSec);
        } catch (JSONException jex) {}
        return json;
    }

    /**
     * Deserialize object from the given JSONObject. The given object is not a container,
     * the object that really contains the data for deserialization
     *
     * @param json
     */
    @Override
    public void deserializeJSON(JSONObject json)
    {
        try
        {
            spentFSec = (float)json.getDouble("s");
            spentSec = -1;
            totalSec = json.getInt("ts");
            act(0);
        } catch (JSONException jex) {}
    }
}
