package northern.captain.quadronia.game;

/**
 * Created by leo on 2/27/15.
 */
public class FieldHoriz extends Field
{
    public FieldHoriz(int sizeX, int sizeY)
    {
        super(sizeX, sizeY);
    }

    /**
     * Sets the radiuses for big circles and upper left corner of the field
     * @param cfg
     * @return
     */
    @Override
    public FieldHoriz setRadius(FieldConfig cfg)
    {
        this.cfg = cfg;

        cornerX = cfg.cornerX;
        cornerY = cfg.cornerY;

        sin3rEI = cfg.circleXShift;
        cos3rEI = cfg.circleDy;
        rE = cfg.outerRadius;
        rEI = cfg.circleDx;
        this.rI = rEI - rE;
        sin3rI = this.rI / 2;
        sin3rE = this.rE / 2;
        cos3rE = Math.round(this.rE * COS3);
        cos3rI = Math.round(this.rI * COS3);

        xC = (cornerX + rE + sin3rEI);
        yC = cornerY + cos3rEI;

        int hei2 = (fieldHei);
        int wid2 = fieldWid / 2;

        pixelWid = ((fieldWid - 3)*sin3rEI + 2*rE);
        pixelHei = hei2*cos3rEI;

        centers = new Center[wid2][hei2];
        centerBoxes = new BoundingBox[wid2][hei2];

        for(int y = 0;y<hei2;y++)
        {
            int py = (yC + y*cos3rEI);
            boolean even = y % 2 == 0;

            for(int x = 0;x< wid2;x++)
            {
                Center point = new Center(
                        even ? (xC + x * rEI)
                             : (xC - sin3rEI + x * rEI),
                        py
                        );
                centers[x][y] = point;
                centerBoxes[x][y] = new BoundingBox(point.x - rE, point.y - cos3rE,
                                                    point.x + rE, point.y + cos3rE);
            }
        }

        return this;
    }

    @Override
    public FieldHoriz build()
    {
        clear();

        //Create cells for the whole field
        for(int y=0;y<fieldHei;y++)
        {
            for(int x=0;x< fieldWid;x++)
            {
                field[x][y] = GameFactory.instance.newCell(x, y, this);
                buildCell(x, y, field[x][y]);
            }
        }

        int lX = fieldWid - 1;

        //Here we enable last right cell in even rows as the extra first one with (-1 index in X axis)
        for(int y = 0;y<fieldHei;y++)
        {
            field[lX][y].cx=-1;
            buildCell(-3, y, field[lX][y]);
            field[lX][y].cx=lX;
        }

        //Disable cells and their faces according to our current cell config
        for(int y=0;y<fieldHei;y++)
        {
            for (int x = 0; x < fieldWid; x++)
            {
                Cell cell = field[x][y];
                cell.faceEnableByBits(cfg.cellFaceEnabled[y][x]);
                //Adding enabled cells to our empty lists
                if(cell.enabled && cell.mask != 0)
                {
                    setCellElement(cell, null);
                }
            }
        }

        //Connect cells to each other
        for(int y=0;y<fieldHei;y++)
        {
            for (int x = 0; x < fieldWid; x++)
            {
                field[x][y].doSelfConnect();
            }
        }

        buildAroundCenters();

        return this;
    }

    @Override
    protected void buildCell(int x, int y, Cell cell)
    {
        int xN, yN;

        if(x % 2 == 0)
        {
            int y2 = y/2;
            y2 *= 2;

            xN = xC + x/2 * rEI;
            yN = yC + y2 * cos3rEI;
        } else
        {
            int y2 = (y+1)/2;
            y2 = y2*2 - 1;

            xN = xC + x/2 * rEI + sin3rEI;
            yN = yC + y2 * cos3rEI;
        }

        cell.build(xN, yN, cfg);

        if(cell.y0 < yN)
        {
            Center center = findCenter(xN, yN);
            if(center != null)
            {
                center.setStartCell(cell);
            }
        }
    }

    private Center findCenter(int xN, int yN)
    {
        int wid2 = fieldWid/2;
        for(int y = 0;y < fieldHei;y++)
        {
            if(centers[0][y].y == yN)
            {
                for(int x = 0;x<wid2;x++)
                {
                    if(centers[x][y].x == xN)
                    {
                        return centers[x][y];
                    }
                }
            }
        }

        return null;
    }

    private void buildAroundCenters()
    {
        int wid2 = fieldWid/2;
        for(int y = 0;y < fieldHei;y++)
        {
            for(int x = 0;x< wid2;x++)
            {
                centers[x][y].buildAround();
            }
        }
    }

}
