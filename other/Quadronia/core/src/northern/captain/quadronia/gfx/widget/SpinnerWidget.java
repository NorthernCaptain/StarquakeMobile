package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public class SpinnerWidget extends Group implements IGraphicsInit
{
    public interface ISpinnable
    {
        void onPlus();
        void onMinus();
    }

    private String name;
    private Label valueLbl;
    private ISpinnable callback;

    public SpinnerWidget(String name)
    {
        this.name = name;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name + "_spin");

        Image bgImg = loader.newImage(name + "_bg", gContext.atlas);
        addActor(bgImg);

        Image plusBut = loader.newImage(name + "_plus", gContext.atlas);
        addActor(plusBut);
        plusBut.setTouchable(Touchable.enabled);
        plusBut.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                plusBut.addAction(Actions.scaleTo(1.3f, 1.3f, 0.2f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                plusBut.addAction(Actions.scaleTo(1, 1, 0.2f));
                if(callback != null) callback.onPlus();
                super.touchUp(event, x, y, pointer, button);
            }
        });

        Image minusBut = loader.newImage(name + "_minus", gContext.atlas);
        addActor(minusBut);
        minusBut.setTouchable(Touchable.enabled);
        minusBut.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                minusBut.addAction(Actions.scaleTo(1.3f, 1.3f, 0.2f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                minusBut.addAction(Actions.scaleTo(1, 1, 0.2f));
                if(callback != null) callback.onMinus();
                super.touchUp(event, x, y, pointer, button);
            }
        });

        valueLbl = loader.newLabel(name + "_val");
        addActor(valueLbl);
    }

    public void setCallback(ISpinnable callback)
    {
        this.callback = callback;
    }

    public void setValue(int value)
    {
        valueLbl.setText(String.valueOf(value));
    }
}
