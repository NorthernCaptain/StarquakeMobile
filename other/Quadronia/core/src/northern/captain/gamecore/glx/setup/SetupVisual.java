package northern.captain.gamecore.glx.setup;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.MyLabel;
import northern.captain.tools.IAnimationFactory;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 11.09.13
 * Time: 23:32
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class SetupVisual extends Table
{
    private Image image;
    private Image bgImage;

    private Label label;

    public static class SetupVisualStyle
    {
        public Drawable mainImage;
        public Drawable offImage;
        public Drawable bgImage;
        public String mainText;
        public String offText;
        public boolean isToggler = false;
    }

    private SetupVisualStyle style;

    public SetupVisual(SetupVisualStyle style)
    {
        this(style, new MyLabel(style.mainText, new Label.LabelStyle()));
    }

    public SetupVisual(SetupVisualStyle style, Label label)
    {
        image = new Image(style.mainImage);

        if(style.bgImage != null)
        {
            bgImage = new Image(style.bgImage);
        }

        this.label = label;
        this.style = style;

        if(style.offImage != null)
        {
            style.isToggler = true;
        }

        if(animOnPress == null)
        {
            animOnPress = new Animations.ScaleUp13();
            animOnRelease = new Animations.ScaleDown1();
        }

        this.setTransform(true);
    }

    public Image getImage()
    {
        return image;
    }

    public Label getLabel()
    {
        return label;
    }

    static IAnimationFactory animOnPress;
    static IAnimationFactory animOnRelease;

    public void create()
    {
        if(bgImage != null)
        {
            Stack frame = new Stack();
            frame.add(bgImage);
            frame.add(image);
            this.add(frame).expand().center().width(bgImage.getWidth()).height(bgImage.getHeight());
        } else
        {
            this.add(image).expand().center().width(image.getWidth()).height(image.getHeight());
        }
        this.row();
        this.add(label).expandX().fillX().center().height(label.getHeight()).padTop(bgImage == null ? 5 : bgImage.getHeight()/10);
        this.pack();

        this.addListener(new ClickListenerPrepared()
        {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                Action action = animOnPress.create(0, new Action()
                {
                    @Override
                    public boolean act(float delta)
                    {
                        clicked(null, 0, 0);
                        return true;
                    }
                });
                addAction(action);
                NContext.current.addTempRefresh();
                return true;
            }

            @Override
            public boolean prepareClicked(InputEvent evt)
            {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                Action action = animOnRelease.create(0);
                addAction(action);
                NContext.current.addTempRefresh();

                if(style.isToggler)
                {
                    value.setBValue(value.getBValue() ? false : true, SetupVisual.this);
                    updateVisual();
                }
                value.activateAction(SetupVisual.this);
            }
        });
    }


    ISetupValue value;

    public void assignValue(ISetupValue value)
    {
        this.value = value;
        if(value != null)
        {
            updateVisual();
        }
    }

    private void updateVisual()
    {
        this.setOrigin(getWidth()/2, getHeight()/2);
        if(style.isToggler)
        {
            final boolean bValue = value.getBValue();
            image.setDrawable(bValue ? style.mainImage : style.offImage);
            label.setText(bValue ? style.mainText : style.offText);
        }
    }

}

