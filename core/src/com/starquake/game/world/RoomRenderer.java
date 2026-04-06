package com.starquake.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.starquake.game.Assets;

public class RoomRenderer {
    private static final int TILE_W = 192;
    private static final int TILE_H = 144;

    // Quad offsets: tl, tr, bl, br — static to avoid per-frame allocation
    private static final int[] QUAD_QCOL = {0, 1, 0, 1};
    private static final int[] QUAD_QROW = {1, 1, 0, 0};
    private static final String[] QUAD_KEY = {"tl", "tr", "bl", "br"};

    private final Assets assets;

    public RoomRenderer(Assets assets) {
        this.assets = assets;
    }

    public void render(SpriteBatch batch, Room room) {
        int[] bpIds = room.bigPlatformIds;
        JsonValue bigPlatforms = assets.metadata.get("big_platforms");

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                JsonValue bp = bigPlatforms.get(bpIds[row * 4 + col]);

                for (int qi = 0; qi < 4; qi++) {
                    int tileIdx = bp.getInt(QUAD_KEY[qi]);
                    int qCol    = QUAD_QCOL[qi];
                    int qRow    = QUAD_QROW[qi];

                    JsonValue tile = (tileIdx < assets.tilesById.length) ? assets.tilesById[tileIdx] : null;
                    if (tile == null) continue;

                    String tileType = tile.getString("type", "empty");
                    if (tileType.equals("empty")) continue;

                    TextureRegion region;
                    if (tileType.equals("fixed")) {
                        region = assets.fixedRegions.get(tileIdx);
                    } else {
                        String key = String.format("tile_%03d_%s", tileIdx, room.color);
                        region = assets.terrainRegions.get(key);
                    }
                    if (region == null) continue;

                    float x = (col * 2 + qCol) * TILE_W;
                    float y = ((2 - row) * 2 + qRow) * TILE_H;
                    batch.draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
                }
            }
        }
    }
}
