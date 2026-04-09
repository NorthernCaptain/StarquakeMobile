package northern.captain.starquake.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonValue;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.objects.GameObject;
import northern.captain.starquake.world.objects.GameObjectRegistry;

import java.util.ArrayList;

/**
 * Room data built from the Atari ST metadata.
 *
 * Each room is a 4×3 grid of big platforms. Each big platform is 2×2 tiles.
 * Total: 256×144 pixels (8×6 tiles, each 32×24).
 *
 * Collision is tile-based:
 * - Solid terrain tiles are full 32×24 collision boxes.
 * - Non-solid tiles (empty, decorative) have no collision.
 * - Special tiles delegate collision to their GameObject.
 * - Temp platforms are checked as an overlay on top.
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

    /** Tile ID grid: [row][col], row 0 = top of room. */
    private final int[][] tileGrid = new int[TILE_ROWS][TILE_COLS];
    private Assets assets;

    /** Game objects indexed by tile key (row*8+col) for O(1) lookup. */
    private final IntMap<Array<GameObject>> objectMap = new IntMap<>();
    /** Flat list for iteration (update/render). */
    private final Array<GameObject> objects = new Array<>();

    /** Active temp platforms — checked as collision overlay. */
    private final ArrayList<TempPlatform> tempPlatforms = new ArrayList<>();

    private FrameBuffer fbo;
    private TextureRegion terrainRegion;

    private Room(int roomIndex, int paletteIndex, int[] bigPlatformIds, Assets assets) {
        this.roomIndex      = roomIndex;
        this.paletteIndex   = paletteIndex;
        this.bigPlatformIds = bigPlatformIds;
        this.assets         = assets;
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
     * Tile-based collision check.
     * Coordinates use libGDX convention: y=0 at bottom.
     */
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < 0 || worldX >= WIDTH || worldY < 0 || worldY >= HEIGHT) return false;

        int col = (int) (worldX / TILE_W);
        int row = (TILE_ROWS - 1) - (int) (worldY / TILE_H);
        if (col >= TILE_COLS || row < 0 || row >= TILE_ROWS) return false;

        // Game objects override all collision for their tile
        Array<GameObject> objs = objectMap.get(GameObject.tileKey(col, row));
        if (objs != null) {
            for (int i = 0, n = objs.size; i < n; i++) {
                if (objs.get(i).isSolidAt(worldX, worldY)) return true;
            }
            return checkTempPlatforms(worldX, worldY);
        }

        // Non-solid tile
        int tileId = tileGrid[row][col];
        if (assets.isTileNonSolid(tileId)) {
            return checkTempPlatforms(worldX, worldY);
        }

        // Regular solid terrain — entire tile is a collision box
        return true;
    }

    private boolean checkTempPlatforms(float worldX, float worldY) {
        if (tempPlatforms.isEmpty()) return false;
        for (int i = 0, n = tempPlatforms.size(); i < n; i++) {
            TempPlatform p = tempPlatforms.get(i);
            if (!p.isExpired()
                    && worldX >= p.x && worldX < p.x + TempPlatform.WIDTH
                    && worldY >= p.y && worldY < p.y + TempPlatform.HEIGHT) {
                return true;
            }
        }
        return false;
    }

    public void addTempPlatform(TempPlatform p) {
        tempPlatforms.add(p);
    }

    public void removeTempPlatform(TempPlatform p) {
        tempPlatforms.remove(p);
    }

    public void clearTempPlatforms() {
        tempPlatforms.clear();
    }

    /** Returns game objects at the given world coordinates, or null. */
    public Array<GameObject> getObjectsAt(float worldX, float worldY) {
        if (worldX < 0 || worldY < 0) return null;
        int col = (int) (worldX / TILE_W);
        int row = (TILE_ROWS - 1) - (int) (worldY / TILE_H);
        if (col >= TILE_COLS || row < 0 || row >= TILE_ROWS) return null;
        return objectMap.get(GameObject.tileKey(col, row));
    }

    /** All game objects in this room. */
    public Array<GameObject> getObjects() {
        return objects;
    }

    public void addObject(GameObject obj) {
        objects.add(obj);
        int key = obj.getTileKey();
        Array<GameObject> list = objectMap.get(key);
        if (list == null) {
            list = new Array<>(2);
            objectMap.put(key, list);
        }
        list.add(obj);
        obj.onAddedToRoom(this);
    }

    public void removeObject(GameObject obj) {
        objects.removeValue(obj, true);
        int key = obj.getTileKey();
        Array<GameObject> list = objectMap.get(key);
        if (list != null) {
            list.removeValue(obj, true);
            if (list.size == 0) objectMap.remove(key);
        }
    }

    public static Room build(Assets assets, int roomIndex) {
        return build(assets, roomIndex, null);
    }

    public static Room build(Assets assets, int roomIndex, GameObjectRegistry registry) {
        JsonValue roomData = assets.getRoom(roomIndex);
        int palette        = roomData.getInt("palette");
        int[] bpIds        = roomData.get("big_platforms").asIntArray();

        Room room = new Room(roomIndex, palette, bpIds, assets);

        for (int bpRow = 0; bpRow < 3; bpRow++) {
            for (int bpCol = 0; bpCol < 4; bpCol++) {
                int bpIdx = bpIds[bpRow * 4 + bpCol];
                JsonValue bp = assets.getBigPlatform(bpIdx);
                if (bp == null) continue;

                for (int qi = 0; qi < 4; qi++) {
                    int tileIdx = bp.getInt(QUAD_KEY[qi]);
                    int tileCol = bpCol * 2 + QUAD_DCOL[qi];
                    int tileRow = bpRow * 2 + QUAD_DROW[qi];

                    room.tileGrid[tileRow][tileCol] = tileIdx;

                    // Spawn game object if this tile is registered
                    if (registry != null && registry.isRegistered(tileIdx)) {
                        GameObject obj = registry.create(tileIdx, assets, tileCol, tileRow);
                        if (obj != null) room.addObject(obj);
                    }
                }
            }
        }

        return room;
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
