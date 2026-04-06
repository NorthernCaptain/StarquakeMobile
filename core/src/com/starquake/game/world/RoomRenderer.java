package com.starquake.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.starquake.game.Assets;

public class RoomRenderer {
    private static final int TILE_W = 192;
    private static final int TILE_H = 144;

    private final Assets assets;
    private final JsonValue metadata;

    public RoomRenderer(Assets assets, JsonValue metadata) {
        this.assets   = assets;
        this.metadata = metadata;
    }

    public void render(SpriteBatch batch, Room room) {
        int[] bpIds = room.bigPlatformIds;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int bpIdx    = bpIds[row * 4 + col];
                JsonValue bp = metadata.get("big_platforms").get(bpIdx);

                int[][] quads = {
                    {bp.getInt("tl"), 0, 1},
                    {bp.getInt("tr"), 1, 1},
                    {bp.getInt("bl"), 0, 0},
                    {bp.getInt("br"), 1, 0}
                };

                for (int[] q : quads) {
                    int tileIdx = q[0];
                    int qCol    = q[1];
                    int qRow    = q[2];

                    JsonValue tile = metadata.get("tiles").get(String.valueOf(tileIdx));
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
