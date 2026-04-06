package northern.captain.quadronia.game;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.res.SharedRes;

/**
 * Created by leo on 17.04.15.
 */
public class FieldConfig
{

    public FieldConfig(int mode)
    {

        int[] sizes = SharedRes.instance.getCommon().getIntArray("fieldsz");
        circleDx = NContext.current.iScale(sizes[0]);
        circleDy = NContext.current.iScale(sizes[1]);
        cornerX = NContext.current.iScale(sizes[2]);
        cornerY = NContext.current.iScale(sizes[3]);
        circleXShift = NContext.current.iScale(sizes[6]);
        outerRadius = NContext.current.iScale(sizes[7]);

        int[] cells = SharedRes.instance.getCommon().getIntArray("cellsz");

        cellCenterDx = NContext.current.iScale(cells[0]);
        cellCenterDy = NContext.current.iScale(cells[1]);
        cellUpLeftX = NContext.current.iScale(cells[2]);
        cellUpLeftY = NContext.current.iScale(cells[3]);
        cellDownLeftY = NContext.current.iScale(cells[4]);

        int[] cellCoord = SharedRes.instance.getCommon().getIntArray("cellcoordsz");

        for(int i=0;i<cellCoord.length;i+=2)
        {
            cellX[i/2] = cellCoord[i];
            cellY[i/2] = cellCoord[i+1];
        }

        switch (mode)
        {
            case ItemFactory.MODE_TWO:
                centersEnabled = FieldLayouts.getLAYOUTS().get(0).centers;
                cellFaceEnabled = FieldLayouts.getLAYOUTS().get(0).cells;
                break;
            case ItemFactory.MODE_TWOTHREE:
                centersEnabled = FieldLayouts.getLAYOUTS().get(5).centers;
                cellFaceEnabled = FieldLayouts.getLAYOUTS().get(5).cells;
                break;

            default:
                centersEnabled = FieldLayouts.getLAYOUTS().get(8).centers;
                cellFaceEnabled = FieldLayouts.getLAYOUTS().get(8).cells;
        }
    }

    public int cornerX;
    public int cornerY;
    /**
     * Mother circle step in X direction
     */
    public int circleDx = 186;
    /**
     * Mother circle step in Y direction
     */
    public int circleDy = 161;
    /**
     * Pixels to shift next row to the right from the previous row
     */
    public int circleXShift = 93;


    /**
     * Outer radius of mother circle
     */
    public int outerRadius = 136;



    /**
     * Delta X of the cell center from the mother center
     */
    public int cellCenterDx = 93;

    /**
     * Delta Y of the cell center from the mother center
     */
    public int cellCenterDy = 54;

    /**
     * Delta X for upper left corner of the sprite we draw in the cell
     */
    public int cellUpLeftX = 160;
    /**
     * Delta Y for upper left corner of the sprite we draw in the cell
     */
    public int cellUpLeftY = 121;
    /**
     * Delta Y for upper left corner of the sprite we draw in the cell for sprite under the mother circle horizon waterline
     */
    public int cellDownLeftY = 13;

    /**
     * Cell point X coordinates relatively of the mother center
     */
    public int[] cellX = new int[6];

    /**
     * Cell point Y coordinates relatively of the mother center
     */
    public int[] cellY = new int[6];

    public int[][] centersEnabled;

    public int[][] cellFaceEnabled;

    public boolean isFullyEnabledCenter(int x, int y)
    {
        if(x < 0 || x >= centersEnabled[0].length
           || y < 0 || y >= centersEnabled.length)
            return false;
        return centersEnabled[y][x] > 2;
    }

    public void setLayout(FieldLayouts.Layout layout)
    {
        centersEnabled = layout.centers;
        cellFaceEnabled = layout.cells;
    }
}
