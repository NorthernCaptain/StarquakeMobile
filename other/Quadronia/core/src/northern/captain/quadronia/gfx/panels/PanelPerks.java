package northern.captain.quadronia.gfx.panels;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.squareup.otto.Subscribe;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.EFullReshuffle;
import northern.captain.quadronia.game.events.EPerkDoHint;
import northern.captain.quadronia.game.events.EPerkShowHint;
import northern.captain.quadronia.game.events.EQuadAreaHitStart;
import northern.captain.quadronia.game.events.EQuadSelect;
import northern.captain.quadronia.game.events.ESolutionFound;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.ICursorEventListener;
import northern.captain.quadronia.gfx.ISimpleDrawable;
import northern.captain.tools.IDisposable;

/**
 * Created by leo on 19.09.15.
 */
public class PanelPerks implements ISimpleDrawable, ICursorEventListener, IDisposable
{
    PerkButton[] buttons = new PerkButton[3];

    PerkButton hintButton;
    PerkButton reshuffleButton;
    PerkButton bombButton;

    Game game;
    ESolutionFound solutionFound;

    public PanelPerks(Game game)
    {
        this.game = game;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        for(PerkButton button : buttons)
        {
            button.draw(batch, parentAlpha);
        }
    }

    @Override
    public void drawFBO(Batch fboBatch, float parentAlpha)
    {

    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        buttons[0] = hintButton = new PerkButton("bhint", game, IGameContext.BONUS_JOKER_IDX)
        {
            @Override
            public void onClick()
            {
                if(game.isPlaying) {
                    NativeNFactory.nci.o_();
                } else {
                    hintButton.setSelected(false);
                    hintButton.update();
                }
            }
        };

        buttons[1] = reshuffleButton = new PerkButton("breshuffle", game, IGameContext.BONUS_SWAP_IDX)
        {
            @Override
            public void onClick()
            {
                if(game.isPlaying) {
                    NativeNFactory.nci.n_();
                } else {
                    reshuffleButton.setSelected(false);
                    reshuffleButton.update();
                }
            }
        };

        buttons[2] = bombButton = new PerkButton("bbomb", game, IGameContext.BONUS_BOMB_IDX)
        {
            @Override
            public void onClick()
            {
                if(game.isPlaying) {
                    game.setTouchMode(Game.TOUCH_MODE_BOMB);
                } else {
                    bombButton.setSelected(false);
                    bombButton.update();
                }
            }
        };

        for(PerkButton button : buttons)
        {
            button.initGraphics(loader, gContext);
        }

        NCore.busRegister(this);
    }

    public void act(float delta)
    {
        for(PerkButton button : buttons)
        {
            button.act(delta);
        }
    }

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
        for(PerkButton button : buttons)
        {
            if(button.doTouchDown(fx, fy))
            {
                for(PerkButton button1 : buttons)
                {
                    if(button1 != button)
                    {
                        button1.setSelected(false);
                    }
                }

                return true;
            }
        }
        return false;
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
        return false;
    }

    @Override
    public void dispose()
    {
        NCore.busUnregister(this);
    }

    @Subscribe
    public void onSolutionFound(ESolutionFound event)
    {
        hintButton.enabled = true;
        solutionFound = event;
    }

    @Subscribe
    public void onQuadAreaHit(EQuadAreaHitStart event)
    {
        hintButton.enabled = false;
        solutionFound = null;
        if(hintButton.isSelected())
        {
            hintButton.setSelected(false);
            hintButton.update();
        }

        if(bombButton.isSelected())
        {
            bombButton.setSelected(false);
            bombButton.update();
        }
    }

    @Subscribe
    public void onFullReshuffle(EFullReshuffle event)
    {
        if(reshuffleButton.isSelected())
        {
            reshuffleButton.setSelected(false);
            reshuffleButton.update();
        }
    }

    @Subscribe
    public void onPerkDoHint(EPerkDoHint event)
    {
        NContext.current.post(()->{
            ESolutionFound solution = solutionFound;
            if(solution != null)
            {
                NCore.busPost(new EPerkShowHint(solution.solution));
            }
            hintButton.enabled = false;
            hintButton.setSelected(false);
            hintButton.update();
        });
    }
}
