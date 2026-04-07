package northern.captain.starquake.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import northern.captain.starquake.Assets;

/**
 * Renders temporary platforms with a dissolve effect when they crumble.
 */
public class PlatformRenderer {
    private final TextureRegion platformRegion;
    private final ShaderProgram dissolveShader;

    public PlatformRenderer(Assets assets) {
        platformRegion = assets.spritesAtlas.findRegion("platform");

        ShaderProgram.pedantic = false;
        dissolveShader = new ShaderProgram(
                Gdx.files.internal("shaders/palette.vert"),
                Gdx.files.internal("shaders/dissolve.frag"));
        if (!dissolveShader.isCompiled())
            Gdx.app.error("PlatformRenderer", "Dissolve shader error:\n" + dissolveShader.getLog());
    }

    /**
     * Render a single platform. Switches to dissolve shader when crumbling.
     * Caller must have batch.begin() active.
     */
    public void render(SpriteBatch batch, TempPlatform platform) {
        float dissolve = platform.getDissolve();
        if (dissolve >= 1f) return;

        if (dissolve > 0) {
            batch.flush();
            batch.setShader(dissolveShader);
            dissolveShader.setUniformf("u_dissolve", dissolve);
            // Screen pixels per game pixel = screen height / viewport height
            float pixelSize = Gdx.graphics.getHeight() / 144f;
            dissolveShader.setUniformf("u_pixelSize", pixelSize);
        }

        batch.draw(platformRegion,
                platform.x, platform.y,
                TempPlatform.WIDTH, TempPlatform.HEIGHT);

        if (dissolve > 0) {
            batch.flush();
            batch.setShader(null);
        }
    }

    public void dispose() {
        dissolveShader.dispose();
    }
}
