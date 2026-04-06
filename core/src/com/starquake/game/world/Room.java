package com.starquake.game.world;

import com.badlogic.gdx.utils.JsonValue;

import java.util.Arrays;

public class Room {
    public final int roomIndex;
    public final String color;
    public final int colorIndex;
    public final int[] bigPlatformIds;   // 12 entries: row-major 4 cols x 3 rows
    public final boolean[][] collision;  // [18 rows][32 cols] of 48px blocks
    public final String[][] behaviorMap; // same grid

    private Room(int roomIndex, String color, int colorIndex,
                 int[] bigPlatformIds, boolean[][] collision, String[][] behaviorMap) {
        this.roomIndex      = roomIndex;
        this.color          = color;
        this.colorIndex     = colorIndex;
        this.bigPlatformIds = bigPlatformIds;
        this.collision      = collision;
        this.behaviorMap    = behaviorMap;
    }

    /** Builds room data from metadata JSON. Call on room entry. */
    public static Room build(JsonValue metadata, int roomIndex) {
        JsonValue roomData = metadata.get("rooms").get(roomIndex);
        String color       = roomData.getString("color");
        int colorIndex     = roomData.getInt("color_index");
        int[] bpIds        = roomData.get("big_platforms").asIntArray();

        boolean[][] collision  = new boolean[18][32];
        String[][] behaviorMap = new String[18][32];
        for (String[] row : behaviorMap)
            Arrays.fill(row, "");

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int bpIdx    = bpIds[row * 4 + col];
                JsonValue bp = metadata.get("big_platforms").get(bpIdx);

                // tl=top-left, tr=top-right, bl=bottom-left, br=bottom-right
                // qCol/qRow are offsets within the big platform (0 or 1)
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
                            if (!solidRow.get(tc).isNull() && solidRow.get(tc).asBoolean())
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
