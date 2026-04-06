package com.starquake.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
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
