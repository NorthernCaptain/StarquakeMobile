package northern.captain.quadronia.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * Created by leo on 13.04.15.
 */
public abstract class FBOBackedWidget extends Widget
{
    protected FrameBuffer fbo;
    protected OrthographicCamera camera;
    protected TextureRegion fboTextureRegion;
    protected Batch fboBatch;
    protected Matrix4 identity = new Matrix4();

    protected boolean needUpdate = true;
    protected int fboVisibleWidth;
    protected int fboVisibleHeight;


    protected void initFBO(int width, int height)
    {
        fboVisibleWidth = width;
        fboVisibleHeight = height;

        int fbWidth = MathUtils.nextPowerOfTwo(width);
        int fbHeight = MathUtils.nextPowerOfTwo(height);

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                fbWidth,
                fbHeight, false);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, fbWidth, fbHeight);
        fboBatch = new SpriteBatch();
    }

    protected void updateFBO(Batch batch)
    {
        needUpdate = false;

        fboTextureRegion = new TextureRegion(fbo.getColorBufferTexture(), fboVisibleWidth, fboVisibleHeight);
        fboTextureRegion.flip(false, true);

        fbo.begin();

        fboBatch.begin();
        fboBatch.setTransformMatrix(identity);
        fboBatch.setProjectionMatrix(camera.combined);

        prepareUpdateFBO();

        drawOnFBO(fboBatch, 1);

        fboBatch.end();
        fbo.end();

        finishUpdateFBO();
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);

        if(needUpdate)
        {
            updateFBO(batch);
        }

        drawOnScreen(batch, parentAlpha);
    }

    protected abstract void drawOnFBO(Batch fboBatch, float parentAlpha);
    protected abstract void drawOnScreen(Batch batch, float parentAlpha);

    protected void prepareUpdateFBO()
    {
        fboBatch.setBlendFunction(-1, -1);
        Gdx.gl20.glBlendFuncSeparate(GL20.GL_SRC_ALPHA,
                GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    protected void finishUpdateFBO()
    {
        fboBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
