package northern.captain.starquake.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.JsonValue;
import northern.captain.starquake.Assets;

/**
 * Renders room terrain tiles into a Room's offscreen buffer using a palette shader.
 *
 * Each Room owns its own FrameBuffer. The renderer draws indexed tiles through the
 * palette shader into whichever Room's FBO is requested. Multiple rooms can have
 * live FBOs simultaneously — needed during scroll transitions where two rooms are
 * partially visible. Rooms release their FBO via {@link Room#dispose()} when they
 * go fully off-screen.
 */
public class RoomRenderer {
    private static final int TILE_W = 32;
    private static final int TILE_H = 24;

    private static final String[] QUAD_KEY = {"tl", "tr", "bl", "br"};
    private static final int[] QUAD_DCOL = {0, 1, 0, 1};
    private static final int[] QUAD_DROW = {0, 0, 1, 1};

    private final Assets assets;
    private final ShaderProgram paletteShader;
    private final SpriteBatch fboBatch;
    private final OrthographicCamera fboCamera;

    public RoomRenderer(Assets assets) {
        this.assets = assets;

        ShaderProgram.pedantic = false;
        paletteShader = new ShaderProgram(
                Gdx.files.internal("shaders/palette.vert"),
                Gdx.files.internal("shaders/palette.frag"));
        if (!paletteShader.isCompiled())
            Gdx.app.error("RoomRenderer", "Shader compile error:\n" + paletteShader.getLog());

        fboBatch = new SpriteBatch();
        fboCamera = new OrthographicCamera();
        fboCamera.setToOrtho(false, Room.WIDTH, Room.HEIGHT);
    }

    /**
     * Ensures the room's terrain is rendered. Returns the RGBA texture region.
     * Only renders on first call per room — subsequent calls return the cached FBO.
     */
    public TextureRegion getTerrainTexture(Room room) {
        if (!room.isRendered()) {
            renderToFbo(room);
        }
        return room.getTerrainRegion();
    }

    private void renderToFbo(Room room) {
        FrameBuffer fbo = room.ensureFbo();
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        assets.paletteTexture.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        fboBatch.setProjectionMatrix(fboCamera.combined);
        fboBatch.setShader(paletteShader);
        fboBatch.begin();
        paletteShader.setUniformi("u_palette", 1);
        paletteShader.setUniformf("u_paletteRow", room.paletteIndex);

        int[] bpIds = room.bigPlatformIds;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int bpIdx = bpIds[row * 4 + col];
                JsonValue bp = assets.getBigPlatform(bpIdx);
                if (bp == null) continue;

                for (int qi = 0; qi < 4; qi++) {
                    int tileIdx = bp.getInt(QUAD_KEY[qi]);
                    TextureRegion region = assets.tileRegions.get(tileIdx);
                    if (region == null) continue;

                    float x = (col * 2 + QUAD_DCOL[qi]) * TILE_W;
                    float y = ((2 - row) * 2 + (1 - QUAD_DROW[qi])) * TILE_H;
                    fboBatch.draw(region, x, y, TILE_W, TILE_H);
                }
            }
        }

        fboBatch.end();
        fboBatch.setShader(null);
        fbo.end();
    }

    public void dispose() {
        paletteShader.dispose();
        fboBatch.dispose();
    }
}
