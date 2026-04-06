package northern.captain.gamecore.glx.shader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import northern.captain.gamecore.glx.tools.MySpriteBatch;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 28.04.13
 * Time: 18:43
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public abstract class CustomShader extends ShaderProgram
{
    public CustomShader(String vertexShader, String fragmentShader)
    {
        super(vertexShader, fragmentShader);
    }

    public CustomShader(FileHandle vertexShader, FileHandle fragmentShader)
    {
        super(vertexShader, fragmentShader);
    }

    public abstract void applyVariables(MySpriteBatch spriteBatch);
}
