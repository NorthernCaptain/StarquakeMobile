package northern.captain.starquake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
    /** Fast non-solid lookup indexed by tile ID. */
    private boolean[] nonSolidTile;
    /** Screen background regions keyed by name */
    public TextureRegion hudScreen, teleportScreen, teleportngScreen, titleScreen, tradingScreen, circuitScreen;

    /** Shared 1×1 white pixel for shader effects (lightning, etc.) */
    public TextureRegion whitePixel;
    /** Palette lookup shader — reusable for drawing indexed tiles outside FBO. */
    public ShaderProgram paletteShader;
    /** Lightning shader used by ElectricShocker. */
    public ShaderProgram lightningShader;

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

        // Build non-solid tile lookup
        int maxTile = metadata.getInt("num_tiles", 122);
        nonSolidTile = new boolean[maxTile];
        JsonValue nsList = metadata.get("non_solid_tiles");
        if (nsList != null) {
            for (JsonValue v = nsList.child; v != null; v = v.next) {
                int id = v.asInt();
                if (id >= 0 && id < maxTile) nonSolidTile[id] = true;
            }
        }

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
        teleportngScreen = screensAtlas.findRegion("teleportng");
        titleScreen = screensAtlas.findRegion("title");
        tradingScreen = screensAtlas.findRegion("trading_pyramid");
        circuitScreen = screensAtlas.findRegion("circuit_board");

        // Font
        TextureAtlas fontAtlas = manager.get("atlases/font.atlas");
        for (Texture t : fontAtlas.getTextures())
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        font = new BitmapFont(Gdx.files.internal("atlases/font.fnt"));
        font.setUseIntegerPositions(true);

        // Shared white pixel for shader effects
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(1, 1, 1, 1);
        px.fill();
        whitePixel = new TextureRegion(new Texture(px));
        px.dispose();

        // Palette shader (for drawing indexed tiles outside the FBO)
        ShaderProgram.pedantic = false;
        paletteShader = new ShaderProgram(
                Gdx.files.internal("shaders/palette.vert"),
                Gdx.files.internal("shaders/palette.frag"));
        if (!paletteShader.isCompiled())
            Gdx.app.error("Assets", "Palette shader error:\n" + paletteShader.getLog());

        // Lightning shader
        lightningShader = new ShaderProgram(
                Gdx.files.internal("shaders/palette.vert"),
                Gdx.files.internal("shaders/lightning.frag"));
        if (!lightningShader.isCompiled())
            Gdx.app.error("Assets", "Lightning shader error:\n" + lightningShader.getLog());

        Gdx.app.log("Assets", "Loaded " + tileRegions.size + " tiles, "
                + spritesAtlas.getRegions().size + " sprites, "
                + itemsAtlas.getRegions().size + " items");
    }

    public JsonValue getRoom(int index) {
        return roomsNode.get(index);
    }

    /** Returns true if this tile has no collision (empty, decorative, pickup, etc). */
    public boolean isTileNonSolid(int tileId) {
        if (tileId < 0 || tileId >= nonSolidTile.length) return true;
        return nonSolidTile[tileId];
    }

    /**
     * Lightweight tile ID lookup from room metadata — no Room object built.
     * Returns the tile ID at (tileCol, tileRow) in the given room, or -1 if invalid.
     * Row 0 = top of room.
     */
    public int getTileIdAt(int roomIndex, int tileCol, int tileRow) {
        if (roomIndex < 0 || roomIndex >= roomsNode.size) return -1;
        if (tileCol < 0 || tileCol >= 8 || tileRow < 0 || tileRow >= 6) return -1;

        JsonValue roomData = roomsNode.get(roomIndex);
        int[] bpIds = roomData.get("big_platforms").asIntArray();
        int bpRow = tileRow / 2;
        int bpCol = tileCol / 2;
        int bpIdx = bpIds[bpRow * 4 + bpCol];

        JsonValue bp = getBigPlatform(bpIdx);
        if (bp == null) return -1;

        int qi = (tileRow % 2) * 2 + (tileCol % 2);
        String[] keys = {"tl", "tr", "bl", "br"};
        return bp.getInt(keys[qi]);
    }

    public JsonValue getBigPlatform(int index) {
        if (index < 0 || index >= bigPlatformsNode.size) return null;
        return bigPlatformsNode.get(index);
    }

    public void dispose() {
        manager.dispose();
        if (paletteTexture != null) paletteTexture.dispose();
        if (font != null) font.dispose();
        if (paletteShader != null) paletteShader.dispose();
        if (lightningShader != null) lightningShader.dispose();
        if (whitePixel != null) whitePixel.getTexture().dispose();
    }
}
