package com.starquake.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class Assets {
    public final AssetManager manager = new AssetManager();

    // Set after loading completes — null before that
    public TextureAtlas fixedAtlas;
    public TextureAtlas spritesAtlas;
    public TextureAtlas fontAtlas;
    public final Map<String, TextureAtlas> terrainAtlases = new HashMap<>();

    /** Game metadata parsed from metadata.json. Available after update() returns true. */
    public JsonValue metadata;
    /** Tiles indexed by integer tile index (0–89) for O(1) lookup. Available after update(). */
    public JsonValue[] tilesById;

    /** Key: region name as packed in atlas, e.g. "tile_014_red" */
    public final Map<String, TextureRegion> terrainRegions = new HashMap<>();
    /** Key: tile index */
    public final IntMap<TextureRegion> fixedRegions = new IntMap<>();
    /** ASCII code 0–127 → glyph region, null if absent */
    public final TextureRegion[] fontRegions = new TextureRegion[128];

    private static final String[] COLORS = {"red", "magenta", "cyan", "yellow", "green", "white"};
    private boolean cachesBuilt = false;

    public Assets() {
        for (String color : COLORS)
            manager.load("atlases/terrain_" + color + ".atlas", TextureAtlas.class);
        manager.load("atlases/fixed.atlas",   TextureAtlas.class);
        manager.load("atlases/sprites.atlas", TextureAtlas.class);
        manager.load("atlases/font.atlas",    TextureAtlas.class);
    }

    /** Called each frame from LoadingScreen. Returns true when fully loaded and cached. */
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

        // Parse metadata once — shared across all screens for the lifetime of the app
        metadata = new JsonReader().parse(Gdx.files.internal("metadata.json"));

        // Pre-index tiles by integer id for O(1) lookup (avoids per-draw string key scan)
        JsonValue tilesNode = metadata.get("tiles");
        tilesById = new JsonValue[90];
        for (int i = 0; i < 90; i++)
            tilesById[i] = tilesNode.get(String.valueOf(i));

        for (String color : COLORS) {
            TextureAtlas atlas = manager.get("atlases/terrain_" + color + ".atlas");
            terrainAtlases.put(color, atlas);
            for (TextureAtlas.AtlasRegion r : atlas.getRegions())
                terrainRegions.put(r.name, r);  // name is already "tile_NNN_color"
        }

        fixedAtlas   = manager.get("atlases/fixed.atlas");
        spritesAtlas = manager.get("atlases/sprites.atlas");
        fontAtlas    = manager.get("atlases/font.atlas");

        for (TextureAtlas.AtlasRegion r : fixedAtlas.getRegions())
            fixedRegions.put(r.index, r);

        for (TextureAtlas.AtlasRegion r : fontAtlas.getRegions())
            if (r.index >= 0 && r.index < 128) fontRegions[r.index] = r;

        Gdx.app.log("Assets", "Loaded " + terrainRegions.size() + " terrain regions, "
                + fixedRegions.size + " fixed regions, "
                + spritesAtlas.getRegions().size + " sprite regions");
    }

    public void dispose() {
        manager.dispose();
    }
}
