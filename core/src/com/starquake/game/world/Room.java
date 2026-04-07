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
 * Each room is a 4×3 grid of big platforms. Each big platform is 2×2 tiles.
 * Total: 256×144 pixels.
 *
 * Collision is pixel-perfect: a 256×144 boolean bitmap built by stamping each
 * tile's pixel data into room coordinates. ~37KB per room.
 */
public class Room {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 144;
    public static final int GRID_COLS = 16;
    public static final int GRID_ROWS = 32;

    public static final int TILE_COLS = 8;
    public static final int TILE_ROWS = 6;
    public static final int TILE_W = 32;
    public static final int TILE_H = 24;

    private static final String[] QUAD_KEY = {"tl", "tr", "bl", "br"};
    private static final int[] QUAD_DCOL = {0, 1, 0, 1};
    private static final int[] QUAD_DROW = {0, 0, 1, 1};

    public final int roomIndex;
    public final int paletteIndex;
    public final int[] bigPlatformIds;

    /** Pixel collision bitmap: [row][col], row 0 = top of room. */
    private final boolean[][] collision;

    private FrameBuffer fbo;
    private TextureRegion terrainRegion;

    private Room(int roomIndex, int paletteIndex, int[] bigPlatformIds, boolean[][] collision) {
        this.roomIndex      = roomIndex;
        this.paletteIndex   = paletteIndex;
        this.bigPlatformIds = bigPlatformIds;
        this.collision      = collision;
    }

    public int getX() { return roomIndex % GRID_COLS; }
    public int getY() { return roomIndex / GRID_COLS; }

    public static int adjacentIndex(int currentIndex, int dx, int dy) {
        int x = currentIndex % GRID_COLS + dx;
        int y = currentIndex / GRID_COLS + dy;
        if (x < 0 || x >= GRID_COLS || y < 0 || y >= GRID_ROWS) return -1;
        return y * GRID_COLS + x;
    }

    /**
     * Pixel-perfect collision check.
     * Coordinates use libGDX convention: y=0 at bottom.
     */
    public boolean isSolidAt(float worldX, float worldY) {
        int px = (int) worldX;
        int py = (HEIGHT - 1) - (int) worldY; // flip: libGDX y=0 bottom → row 0 top
        if (px < 0 || px >= WIDTH || py < 0 || py >= HEIGHT) return false;
        return collision[py][px];
    }

    /**
     * Stamp a rectangular block of solid pixels into the collision bitmap.
     * Coordinates use libGDX convention (y=0 at bottom).
     */
    public void setCollisionRect(int worldX, int worldY, int w, int h, boolean solid) {
        for (int dy = 0; dy < h; dy++) {
            int py = (HEIGHT - 1) - (worldY + dy);
            if (py < 0 || py >= HEIGHT) continue;
            for (int dx = 0; dx < w; dx++) {
                int px = worldX + dx;
                if (px >= 0 && px < WIDTH) {
                    collision[py][px] = solid;
                }
            }
        }
    }

    public static Room build(Assets assets, int roomIndex) {
        JsonValue roomData = assets.getRoom(roomIndex);
        int palette        = roomData.getInt("palette");
        int[] bpIds        = roomData.get("big_platforms").asIntArray();

        // Build pixel collision bitmap by stamping tile pixel data
        boolean[][] collision = new boolean[HEIGHT][WIDTH];
        for (int bpRow = 0; bpRow < 3; bpRow++) {
            for (int bpCol = 0; bpCol < 4; bpCol++) {
                int bpIdx = bpIds[bpRow * 4 + bpCol];
                JsonValue bp = assets.getBigPlatform(bpIdx);
                if (bp == null) continue;

                for (int qi = 0; qi < 4; qi++) {
                    int tileIdx = bp.getInt(QUAD_KEY[qi]);
                    boolean[][] tilePx = assets.getTilePixels(tileIdx);
                    if (tilePx == null) continue;

                    int tileCol = bpCol * 2 + QUAD_DCOL[qi];
                    int tileRow = bpRow * 2 + QUAD_DROW[qi];
                    int baseX = tileCol * TILE_W;
                    int baseY = tileRow * TILE_H;

                    int th = tilePx.length;
                    int tw = tilePx[0].length;
                    for (int ty = 0; ty < th && baseY + ty < HEIGHT; ty++) {
                        for (int tx = 0; tx < tw && baseX + tx < WIDTH; tx++) {
                            if (tilePx[ty][tx]) {
                                collision[baseY + ty][baseX + tx] = true;
                            }
                        }
                    }
                }
            }
        }

        return new Room(roomIndex, palette, bpIds, collision);
    }

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

    public TextureRegion getTerrainRegion() {
        return terrainRegion;
    }

    public boolean isRendered() {
        return fbo != null;
    }

    public void dispose() {
        if (fbo != null) {
            fbo.dispose();
            fbo = null;
            terrainRegion = null;
        }
    }
}
