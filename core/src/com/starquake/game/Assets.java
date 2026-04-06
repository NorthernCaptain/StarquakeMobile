package com.starquake.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;

import java.util.HashMap;
import java.util.Map;

public class Assets {
    public final AssetManager manager = new AssetManager();

    public TextureAtlas fixedAtlas;
    public TextureAtlas spritesAtlas;
    public TextureAtlas fontAtlas;
    public final Map<String, TextureAtlas> terrainAtlases = new HashMap<>();

    /** Key: region name as packed, e.g. "tile_014_red" */
    public final Map<String, TextureRegion> terrainRegions = new HashMap<>();
    /** Key: tile index */
    public final IntMap<TextureRegion> fixedRegions = new IntMap<>();
    /** ASCII index 0-127 → glyph, null if absent */
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
