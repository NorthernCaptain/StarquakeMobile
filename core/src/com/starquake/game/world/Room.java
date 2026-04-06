package com.starquake.game.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.JsonValue;
import com.starquake.game.Assets;

/**
 * Room data built from the Atari ST metadata.
 *
 * Each room contains 12 big-platform indices (4 cols × 3 rows).
 * Each big platform contains 4 tile indices (TL, TR, BL, BR).
 * Palette index determines the room's color scheme (0–25).
 *
 * The room owns its terrain FrameBuffer. Call {@link #dispose()} when the
 * room is no longer visible (e.g. after a transition completes).
 */
public class Room {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 144;
    public static final int GRID_COLS = 16;
    public static final int GRID_ROWS = 32;

    public final int roomIndex;
    public final int paletteIndex;
    public final int[] bigPlatformIds;

    private FrameBuffer fbo;
    private TextureRegion terrainRegion;

    private Room(int roomIndex, int paletteIndex, int[] bigPlatformIds) {
        this.roomIndex      = roomIndex;
        this.paletteIndex   = paletteIndex;
        this.bigPlatformIds = bigPlatformIds;
    }

    public int getX() { return roomIndex % GRID_COLS; }
    public int getY() { return roomIndex / GRID_COLS; }

    /**
     * Returns the room index adjacent to {@code currentIndex} in direction (dx, dy).
     * Returns -1 if out of bounds.
     */
    public static int adjacentIndex(int currentIndex, int dx, int dy) {
        int x = currentIndex % GRID_COLS + dx;
        int y = currentIndex / GRID_COLS + dy;
        if (x < 0 || x >= GRID_COLS || y < 0 || y >= GRID_ROWS) return -1;
        return y * GRID_COLS + x;
    }

    public static Room build(Assets assets, int roomIndex) {
        JsonValue roomData = assets.getRoom(roomIndex);
        int palette        = roomData.getInt("palette");
        int[] bpIds        = roomData.get("big_platforms").asIntArray();
        return new Room(roomIndex, palette, bpIds);
    }

    /** Returns the FBO for rendering terrain into. Creates on first call. */
    public FrameBuffer ensureFbo() {
        if (fbo == null) {
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, WIDTH, HEIGHT, false);
            fbo.getColorBufferTexture().setFilter(
                    Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            terrainRegion = new TextureRegion(fbo.getColorBufferTexture());
            terrainRegion.flip(false, true);
        }
        return fbo;
    }

    /** Returns the terrain as an RGBA texture region. Null if not yet rendered. */
    public TextureRegion getTerrainRegion() {
        return terrainRegion;
    }

    /** Returns true if terrain has been rendered into the FBO. */
    public boolean isRendered() {
        return fbo != null;
    }

    /** Releases the FBO. Call when the room goes fully off-screen. */
    public void dispose() {
        if (fbo != null) {
            fbo.dispose();
            fbo = null;
            terrainRegion = null;
        }
    }
}
