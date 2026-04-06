package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 01.05.13
 * Time: 23:06
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class ButtonImage extends Table
{
    private Image buttonImage;

    public Image getButtonImage()
    {
        return buttonImage;
    }

    public void setButtonImage(Image buttonImage)
    {
        this.buttonImage = buttonImage;
    }
}
