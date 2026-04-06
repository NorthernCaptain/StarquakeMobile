package com.starquake.game.world;

import com.badlogic.gdx.utils.JsonValue;
import com.starquake.game.Assets;

import java.util.Arrays;

public class Room {
    public final int roomIndex;
    public final String color;
    public final int colorIndex;
    public final int[] bigPlatformIds;   // 12 entries: row-major 4 cols x 3 rows
    public final boolean[][] collision;  // [18 rows][32 cols] of 48px blocks
    public final String[][] behaviorMap; // same grid

    // Quad offsets: tl, tr, bl, br
    private static final int[] QUAD_QCOL = {0, 1, 0, 1};
    private static final int[] QUAD_QROW = {1, 1, 0, 0};
    private static final String[] QUAD_KEY = {"tl", "tr", "bl", "br"};

    private Room(int roomIndex, String color, int colorIndex,
                 int[] bigPlatformIds, boolean[][] collision, String[][] behaviorMap) {
        this.roomIndex      = roomIndex;
        this.color          = color;
        this.colorIndex     = colorIndex;
        this.bigPlatformIds = bigPlatformIds;
        this.collision      = collision;
        this.behaviorMap    = behaviorMap;
    }

    /** Builds room data from pre-loaded assets. Call on room entry. */
    public static Room build(Assets assets, int roomIndex) {
        JsonValue roomData = assets.metadata.get("rooms").get(roomIndex);
        String color       = roomData.getString("color");
        int colorIndex     = roomData.getInt("color_index");
        int[] bpIds        = roomData.get("big_platforms").asIntArray();

        boolean[][] collision  = new boolean[18][32];
        String[][] behaviorMap = new String[18][32];
        for (String[] row : behaviorMap)
            Arrays.fill(row, "");

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

                    String behavior    = tile.getString("behavior", "");
                    JsonValue solidity = tile.get("solidity");
                    if (solidity == null || solidity.size == 0) continue;

                    // Base block in 32-wide x 18-tall grid (each block = 48px)
                    // 4 blocks per tile width, 3 blocks per tile height
                    // Y is flipped: row 0 (top of room in data) → row 12 in libGDX coords
                    int baseCol = (col * 2 + qCol) * 4;
                    int baseRow = (2 - row) * 6 + (1 - qRow) * 3;

                    for (int tr = 0; tr < solidity.size; tr++) {
                        JsonValue solidRow = solidity.get(tr);
                        for (int tc = 0; tc < solidRow.size; tc++) {
                            int cr = baseRow + tr;
                            int cc = baseCol + tc;
                            if (cr < 0 || cr >= 18 || cc < 0 || cc >= 32) continue;
                            JsonValue cell = solidRow.get(tc);
                            if (cell.isNull()) continue;  // skip empty cells in sparse tiles
                            if (cell.asBoolean())
                                collision[cr][cc] = true;
                            if (!behavior.isEmpty())
                                behaviorMap[cr][cc] = behavior;
                        }
                    }
                }
            }
        }

        return new Room(roomIndex, color, colorIndex, bpIds, collision, behaviorMap);
    }
}
