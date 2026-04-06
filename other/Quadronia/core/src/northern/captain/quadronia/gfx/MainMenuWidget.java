package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

import northern.captain.quadronia.game.IGameContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 16.05.15.
 */
public class MainMenuWidget extends Actor implements IGraphicsInit, ICursorEventListener
{
    northern.captain.quadronia.gfx.panels.PButton[] difficultyButtons = new northern.captain.quadronia.gfx.panels.PButton[3];
    IGameContext gameContext;

    boolean enabled = true;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public MainMenuWidget(IGameContext gameContext)
    {
        difficultyButtons[0] = new northern.captain.quadronia.gfx.panels.PButton("easy").setCheckMode(true);
        difficultyButtons[1] = new northern.captain.quadronia.gfx.panels.PButton("medium").setCheckMode(true);
        difficultyButtons[2] = new northern.captain.quadronia.gfx.panels.PButton("hard").setCheckMode(true);

        this.gameContext = gameContext;
        this.setPosition(0, 0);
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        int mode = gameContext.getLastMode();
        for(int i=0;i<difficultyButtons.length;i++)
        {
            difficultyButtons[i].initGraphics(loader, gContext);
            difficultyButtons[i].setChecked(i == mode);
        }
    }

    @Override
    public void act(float delta)
    {
        if(!enabled) return;

        super.act(delta);
        for(int i=0;i<difficultyButtons.length;i++)
        {
            difficultyButtons[i].act(delta);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        if(!enabled) return;

        for(int i=0;i<difficultyButtons.length;i++)
        {
            difficultyButtons[i].draw(batch, parentAlpha);
        }
    }


    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        if(!enabled) return false;

        for(int i=0;i<difficultyButtons.length;i++)
        {
            if(difficultyButtons[i].doTouchDown(fx, fy))
            {
                return true;
            }
        }
        return  false;
    }

    @Override
    public boolean doDrag(int fx, int fy)
    {
        return false;
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        if(!enabled) return false;

        for(int i=0;i<difficultyButtons.length;i++)
        {
            northern.captain.quadronia.gfx.panels.PButton button = difficultyButtons[i];
            if(button.doTouchUp(fx, fy))
            {
                button.setChecked(true);
                for(int j=0;j<difficultyButtons.length;j++)
                {
                    if(i != j)
                    {
                        difficultyButtons[j].setChecked(false);
                    }
                }

                gameContext.setLastMode(i);

                return true;
            }
        }
        return  false;
    }
}
