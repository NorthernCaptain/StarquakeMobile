package northern.captain.quadronia.game;

import northern.captain.tools.Helpers;

/**
 * Created by leo on 09.03.15.
 */
public class ItemFactory
{
    public static final int MODE_TWO = 0;
    public static final int MODE_TWOTHREE = 1;
    public static final int MODE_THREE = 2;
    public static final int MODE_FOUR = 3;
    public static final int MODE_ALL = 4;

    public static final int[] LEVELUP_MODES = new int[]
            {
                    MODE_THREE, //for levelup in MODE_TWO
                    MODE_FOUR,  //for levelup in MODE_THREE
                    MODE_FOUR,  //for levelup in MODE_TWOTHREE
                    MODE_FOUR
            };

    public static final ItemFactory instance = new ItemFactory();

    private static final int[] ITEM2N1 = new int[]
            {
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_SOUTH
            };

    private static final int[] ITEM2N2 = new int[]
            {
                    Face.FC_NW, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE
            };

    private static final int[] ITEM3N1 = new int[]
            {
                    Face.FC_NW, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_SOUTH
            };

    private static final int[] ITEM3N2 = new int[]
            {
                    Face.FC_NW, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_NE
            };

    private static final int[] ITEM3N3 = new int[]
            {
                    Face.FC_SOUTH, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_SOUTH
            };

    private static final int[] ITEM3N4 = new int[]
            {
                    Face.FC_SOUTH, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_NE
            };

    private static final int[] ITEM4N1 = new int[]
            {
                    Face.FC_SOUTH, Face.FC_NW,
                    Face.FC_SE, Face.FC_NORTH,
                    Face.FC_SOUTH, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
            };

    private static final int[] ITEM4N2 = new int[]
            {
                    Face.FC_SOUTH, Face.FC_NW,
                    Face.FC_SE, Face.FC_NORTH,
                    Face.FC_SOUTH, Face.FC_NE,
                    Face.FC_SW, Face.FC_NORTH,
            };

    private static final int[] ITEM4N3 = new int[]
            {
                    Face.FC_SOUTH, Face.FC_NW,
                    Face.FC_SE, Face.FC_SW,
                    Face.FC_NE, Face.FC_NW,
                    Face.FC_SE, Face.FC_NORTH,
            };

    private static final int[] ITEM4N4 = new int[]
            {
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_NE,
            };

    private static final int[] ITEM4N5 = new int[]
            {
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_SOUTH,
            };

    private static final int[][] ITEMS2 = new int[][]
            {
                    ITEM2N1, ITEM2N2,
            };

    private static final int[][] ITEMS3 = new int[][]
    {
        ITEM3N1, ITEM3N2, ITEM3N3,
    };

    private static final int[][] ITEMS4 = new int[][]
            {
                    ITEM4N1, ITEM4N2, ITEM4N3, ITEM4N5, ITEM4N5,
            };

    private static final int[][] ITEMS3P2 = new int[][]
            {
                    ITEM3N1, ITEM2N1, ITEM3N3, ITEM2N2, ITEM3N2,
            };

    private static final int[][] ALL_ITEMS = new int[][]
            {
                    ITEM2N1, ITEM2N2, ITEM3N1, ITEM3N2, ITEM3N3, ITEM4N1, ITEM4N2, ITEM4N3, ITEM4N4, ITEM4N5
            };


    private static final int[][][] ITEMS = new int[][][]
            {
                    ITEMS2,
                    ITEMS3P2,
                    ITEMS3,
                    ITEMS4,
                    ALL_ITEMS,
            };

    private static final String[] ITEM_NAMES2 = new String[]
            {
                    "item2n1",
                    "item2n2",
            };

    private static final String[] ITEM_NAMES3 = new String[]
            {
                    "item3n1",
                    "item3n2",
                    "item3n3",
            };

    private static final String[] ITEM_NAMES3P2 = new String[]
            {
                    "item3n1",
                    "item2n1",
                    "item3n3",
                    "item2n2",
                    "item3n2",
            };

    private static final String[] ITEM_NAMES4 = new String[]
            {
                    "item4n1",
                    "item4n2",
                    "item4n3",
                    "item4n4",
                    "item4n5",
            };

    private static final String[] ALL_ITEM_NAMES = new String[]
            {
                    "item2n1",
                    "item2n2",
                    "item3n1",
                    "item3n2",
                    "item3n3",
                    "item4n1",
                    "item4n2",
                    "item4n3",
                    "item4n4",
                    "item4n5",
            };

    private static final String[][] ITEM_NAMES = new String[][]
            {
                    ITEM_NAMES2,
                    ITEM_NAMES3P2,
                    ITEM_NAMES3,
                    ITEM_NAMES4,
                    ALL_ITEM_NAMES
            };

    public Item getItem(int mode, int idx)
    {
        int[][] items = ITEMS[mode];
        int[] nodes = items[idx];
        int allIdx = 0;
        for(int i=0;i<ALL_ITEMS.length;i++)
        {
            if(ALL_ITEMS[i] == nodes)
            {
                allIdx = i;
                break;
            }
        }
        return new Item(nodes, allIdx, mode);
    }

    public String[] getItemNames(int mode)
    {
        return ITEM_NAMES[mode];
    }
}
