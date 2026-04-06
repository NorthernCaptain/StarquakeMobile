package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public class TripleButton extends Group implements IGraphicsInit
{
    private String name;
    private Image bgImg;
    private Image backImg;
    private Image frontImg;
    private Label textLbl;
    private Container textCont;

    private float backX, backY;

    private Runnable onClick;

    public TripleButton(String name)
    {
        this.name = name;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name + "but");

        bgImg = loader.newImage(name + "bg", gContext.atlas);
        if(bgImg != null) addActor(bgImg);

        backImg = loader.newImage(name + "back", gContext.atlas);
        if(backImg != null)
        {
            addActor(backImg);
            backImg.setOrigin(backImg.getWidth()/2, backImg.getHeight()/2);
            backX = backImg.getX();
            backY = backImg.getY();
        }

        frontImg = loader.newImage(name + "front", gContext.atlas);
        if(frontImg != null) addActor(frontImg);

        textLbl = loader.newLabelNullable(name + "text");
        if(textLbl != null)
        {
            textCont = new Container(textLbl);
            textCont.setTransform(true);
            loader.applyTo(textCont, name + "text");
            textCont.setOrigin(textCont.getWidth()/2.0f, textCont.getHeight()/2.0f);
            textCont.fill();
            addActor(textCont);
        }

        this.addListener(new ClickListenerPrepared()
        {
            @Override
            public boolean prepareClicked(InputEvent evt)
            {
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                boolean ret = super.touchDown(event, x, y, pointer, button);
                if(ret)
                {
                    setAnimationOut();
                }

                return ret;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                super.touchUp(event, x, y, pointer, button);
                setAnimationIn();
            }

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if(onClick != null) onClick.run();
            }
        });
    }

    private void setAnimationOut()
    {
        if(backImg != null) {
            backImg.addAction(Actions.scaleTo(1.3f, 1.3f, 0.3f, Interpolation.pow3Out));
        }
        if(frontImg != null) frontImg.addAction(Actions.scaleTo(1.3f, 1.3f, 0.3f, Interpolation.pow3Out));
        if(textLbl != null) textCont.addAction(Actions.scaleTo(1.3f, 1.3f, 0.3f, Interpolation.pow3Out));
    }

    private void setAnimationIn()
    {
        if(backImg != null) {
            backImg.addAction(Actions.scaleTo(1.f, 1.f, 0.3f, Interpolation.pow3In));
        }
        if(frontImg != null) frontImg.addAction(Actions.scaleTo(1.f, 1.f, 0.3f, Interpolation.pow3In));
        if(textLbl != null) textCont.addAction(Actions.scaleTo(1.f, 1.f, 0.3f, Interpolation.pow3In));
    }

    public void setText(String text)
    {
        if(textLbl != null) textLbl.setText(text);
    }

    public void setOnClick(Runnable onClick)
    {
        this.onClick = onClick;
    }
}
