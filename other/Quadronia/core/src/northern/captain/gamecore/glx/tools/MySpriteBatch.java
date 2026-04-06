package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import northern.captain.gamecore.glx.shader.CustomShader;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 28.04.13
 * Time: 15:54
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class MySpriteBatch extends SpriteBatch
{
    public Texture currentTexture = null;
    private CustomShader currentShader = null;

    public boolean inDrawing = false;

    public MySpriteBatch(CustomShader shaderP)
    {
        super(1000, shaderP);
        currentShader = shaderP;
    }

    private void switchToTexture(Texture texture)
    {
        currentTexture = texture;
        if(currentShader != null && currentTexture != null)
        {
            currentShader.applyVariables(this);
        }
    }

    @Override
    public void setProjectionMatrix(Matrix4 projection)
    {
        super.setProjectionMatrix(projection);    //To change body of overridden methods use File | Settings | File Templates.
        if(inDrawing && currentTexture!=null)
        {
            currentShader.applyVariables(this);
        }
    }

    @Override
    public void setTransformMatrix(Matrix4 transform)
    {
        super.setTransformMatrix(transform);    //To change body of overridden methods use File | Settings | File Templates.
        if(inDrawing && currentTexture!=null)
        {
            currentShader.applyVariables(this);
        }
    }

    @Override
    public void begin()
    {
        super.begin();    //To change body of overridden methods use File | Settings | File Templates.
        currentTexture = null;
        inDrawing = true;
    }

    @Override
    public void end()
    {
        super.end();    //To change body of overridden methods use File | Settings | File Templates.
        currentTexture = null;
        inDrawing = false;
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean clockwise)
    {
        Texture texture = region.getTexture();
        boolean doSet = texture != currentTexture;
        super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation)
    {
        Texture texture = region.getTexture();
        boolean doSet = texture != currentTexture;
        super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height)
    {
        Texture texture = region.getTexture();
        boolean doSet = texture != currentTexture;
        super.draw(region, x, y, width, height);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(TextureRegion region, float x, float y)
    {
        Texture texture = region.getTexture();
        boolean doSet = texture != currentTexture;
        super.draw(region, x, y);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int length)
    {
        boolean doSet = texture != currentTexture;
        super.draw(texture, spriteVertices, offset, length);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height)
    {
        boolean doSet = texture != currentTexture;
        super.draw(texture, x, y, width, height);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(Texture texture, float x, float y)
    {
        boolean doSet = texture != currentTexture;
        super.draw(texture, x, y);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2)
    {
        boolean doSet = texture != currentTexture;
        super.draw(texture, x, y, width, height, u, v, u2, v2);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight)
    {
        boolean doSet = texture != currentTexture;
        super.draw(texture, x, y, srcX, srcY, srcWidth, srcHeight);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY)
    {
        boolean doSet = texture != currentTexture;
        super.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }

    @Override
    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY)
    {
        boolean doSet = texture != currentTexture;
        super.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY);    //To change body of overridden methods use File | Settings | File Templates.
        if(doSet)
            switchToTexture(texture);
    }
}
