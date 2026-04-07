package northern.captain.starquake.world.objects;

import northern.captain.starquake.Assets;

/**
 * A tile with multiple solid rectangles.
 * Each rect is defined as (xOffset, yOffset, width, height) from the tile's bottom-left.
 */
public class MultiCollisionTile extends GameObject {
    private final float[][] rects; // [n][4]: left, bottom, right, top in world coords

    /**
     * @param localRects array of {xOffset, yOffset, width, height} relative to tile bottom-left
     */
    public MultiCollisionTile(Assets assets, int tileCol, int tileRow, int[][] localRects) {
        super(assets, tileCol, tileRow);
        rects = new float[localRects.length][4];
        for (int i = 0; i < localRects.length; i++) {
            rects[i][0] = x + localRects[i][0];              // left
            rects[i][1] = y + localRects[i][1];              // bottom
            rects[i][2] = x + localRects[i][0] + localRects[i][2]; // right
            rects[i][3] = y + localRects[i][1] + localRects[i][3]; // top
        }
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        for (float[] r : rects) {
            if (worldX >= r[0] && worldX < r[2] && worldY >= r[1] && worldY < r[3]) {
                return true;
            }
        }
        return false;
    }
}
