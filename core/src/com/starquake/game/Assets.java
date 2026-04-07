package com.starquake.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class Assets {
    public final AssetManager manager = new AssetManager();

    /** Tile index map atlas — grayscale, pixel value = palette index * 17 */
    public TextureAtlas tilesAtlas;
    public TextureAtlas spritesAtlas;
    public TextureAtlas itemsAtlas;
    public TextureAtlas screensAtlas;

    /** 16×26 palette lookup texture (row = palette index, col = color index) */
    public Texture paletteTexture;

    /** BMFont built from the font atlas */
    public BitmapFont font;

    /** Tile regions keyed by tile index */
    public final IntMap<TextureRegion> tileRegions = new IntMap<>();
    /** Per-pixel collision data for each tile. Indexed by tile ID. null = fully empty. */
    private boolean[][][] tilePixels; // [tileId][row][col] — row 0 = top of tile
    /** Screen background regions keyed by name */
    public TextureRegion hudScreen, teleportScreen, titleScreen, tradingScreen, circuitScreen;

    private JsonValue metadata;
    private JsonValue roomsNode;
    private JsonValue bigPlatformsNode;

    private boolean cachesBuilt = false;

    public Assets() {
        manager.load("atlases/tiles.atlas", TextureAtlas.class);
        manager.load("atlases/sprites.atlas", TextureAtlas.class);
        manager.load("atlases/items.atlas", TextureAtlas.class);
        manager.load("atlases/screens.atlas", TextureAtlas.class);
        manager.load("atlases/font.atlas", TextureAtlas.class);
    }

    public boolean update() {
        if (!manager.update()) return false;
        if (!cachesBuilt) buildCaches();
        return true;
    }

    public float getProgress() {
        return manager.getProgress();
    }

    private void buildCaches() {
        cachesBuilt = true;

        metadata = new JsonReader().parse(Gdx.files.internal("metadata.json"));
        roomsNode = metadata.get("rooms");
        bigPlatformsNode = metadata.get("big_platforms");

        // Tile index map atlas — set nearest filtering for pixel-perfect palette lookup
        tilesAtlas = manager.get("atlases/tiles.atlas");
        for (Texture t : tilesAtlas.getTextures())
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        for (TextureAtlas.AtlasRegion r : tilesAtlas.getRegions())
            tileRegions.put(r.index, r);

        // Build per-pixel collision data from tile atlas
        // Read the atlas page as a Pixmap to access raw pixel data
        int maxTile = metadata.getInt("num_tiles", 122);
        tilePixels = new boolean[maxTile][][];
        java.util.Set<Integer> nonSolid = new java.util.HashSet<>();
        JsonValue nsList = metadata.get("non_solid_tiles");
        if (nsList != null) {
            for (JsonValue v = nsList.child; v != null; v = v.next)
                nonSolid.add(v.asInt());
        }
        // Extract pixel data from the atlas texture using TextureData
        Texture atlasTexture = tilesAtlas.getTextures().first();
        if (!atlasTexture.getTextureData().isPrepared())
            atlasTexture.getTextureData().prepare();
        Pixmap atlasPixmap = atlasTexture.getTextureData().consumePixmap();
        for (int id = 0; id < maxTile; id++) {
            if (nonSolid.contains(id)) continue;
            TextureRegion r = tileRegions.get(id);
            if (r == null) continue;
            int rw = r.getRegionWidth();
            int rh = r.getRegionHeight();
            boolean[][] px = new boolean[rh][rw];
            boolean hasAny = false;
            for (int ty = 0; ty < rh; ty++) {
                for (int tx = 0; tx < rw; tx++) {
                    int pixel = atlasPixmap.getPixel(r.getRegionX() + tx, r.getRegionY() + ty);
                    // Grayscale index map: any non-zero value = has content = solid
                    if ((pixel >>> 24) > 0 && (pixel & 0xFF) > 0) {
                        px[ty][tx] = true;
                        hasAny = true;
                    }
                }
            }
            if (hasAny) tilePixels[id] = px;
        }
        atlasPixmap.dispose();

        // Palette texture — loaded directly (not through atlas)
        paletteTexture = new Texture(Gdx.files.internal("palettes.png"), false);
        paletteTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Sprites
        spritesAtlas = manager.get("atlases/sprites.atlas");
        for (Texture t : spritesAtlas.getTextures())
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Items
        itemsAtlas = manager.get("atlases/items.atlas");
        for (Texture t : itemsAtlas.getTextures())
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Screens
        screensAtlas = manager.get("atlases/screens.atlas");
        for (Texture t : screensAtlas.getTextures())
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hudScreen = screensAtlas.findRegion("hud_gameplay");
        teleportScreen = screensAtlas.findRegion("teleport");
        titleScreen = screensAtlas.findRegion("title");
        tradingScreen = screensAtlas.findRegion("trading_pyramid");
        circuitScreen = screensAtlas.findRegion("circuit_board");

        // Font
        TextureAtlas fontAtlas = manager.get("atlases/font.atlas");
        for (Texture t : fontAtlas.getTextures())
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        font = new BitmapFont(Gdx.files.internal("atlases/font.fnt"));
        font.setUseIntegerPositions(true);

        Gdx.app.log("Assets", "Loaded " + tileRegions.size + " tiles, "
                + spritesAtlas.getRegions().size + " sprites, "
                + itemsAtlas.getRegions().size + " items");
    }

    public JsonValue getRoom(int index) {
        return roomsNode.get(index);
    }

    /**
     * Returns the per-pixel collision bitmap for a tile, or null if the tile
     * is fully passable. Array is [row][col], row 0 = top of tile.
     */
    public boolean[][] getTilePixels(int tileIndex) {
        if (tileIndex < 0 || tileIndex >= tilePixels.length) return null;
        return tilePixels[tileIndex];
    }

    public JsonValue getBigPlatform(int index) {
        if (index < 0 || index >= bigPlatformsNode.size) return null;
        return bigPlatformsNode.get(index);
    }

    public void dispose() {
        manager.dispose();
        if (paletteTexture != null) paletteTexture.dispose();
        if (font != null) font.dispose();
    }
}
