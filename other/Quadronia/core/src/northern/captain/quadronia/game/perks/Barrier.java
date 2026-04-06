package northern.captain.quadronia.game.perks;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.quadronia.game.GeoFace;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 02.04.15.
 */
public class Barrier implements IFieldPerk
{
    private Cell cell;
    private int  faceIndex;
    private boolean active;

    public float cx, cy;
    public float rotation;
    private int idx = 0;


    public Barrier() {}
    public Barrier(int idx)
    {
        this.idx = idx;
    }

    public int getIdx()
    {
        return idx;
    }

    /**
     * Find a place where this perk can be set to the field
     * @param field
     * @return true if the perk was put to the field, false otherwise
     */
    @Override
    public boolean putToField(Engine game, Field field)
    {
        int width = field.fieldWid - 2;
        int height = field.fieldHei - 2;

        int x, y;
        int side;
        for(int i=0; i < 10;i++)
        {
            x = Helpers.RND.nextInt(width);
            y = Helpers.RND.nextInt(height);

            cell = field.cell(x, y);
            if(cell == null) continue;

            if(cell.hasElement() || cell.hasPerk())
            {
                continue;
            }

            faceIndex = side = Helpers.RND.nextInt(3);

            if(isCellAccepted(cell))
            {
                Cell connected = cell.connected[side];
                if(!connected.hasElement() && !connected.hasPerk())
                {
                    activate(side);
                    game.setIFieldPerk(this);
                    return true;
                }
            }
        }
        active = false;
        return false;
    }

    protected boolean isCellAccepted(Cell cell)
    {
        return cell.faces[faceIndex].enabled &&
              !cell.faces[faceIndex].hasPerk() &&
               cell.connected[faceIndex] != null;
    }

    @Override
    public boolean removeFromField(Engine game)
    {
        active = false;
        GeoFace face = cell.faces[faceIndex];

        cx = face.cx;
        cy = face.cy;

        cell.faceEnable(face.type, true);
        rotation = Face.SIDE_ANGLE[face.type];
        face.setPerk(null);

        Cell connected = cell.connected[faceIndex];
        int ftype = Face.SIDE_CONNECTOR[face.type];
        connected.faceEnable(ftype, true);
        Face conFace = connected.faces[ftype/2];
        conFace.setPerk(null);

        game.unsetIFieldPerk(this);
        return true;
    }

    private void activate(int side)
    {
        faceIndex = side;
        active = true;
        GeoFace face = cell.faces[faceIndex];

        cx = face.cx;
        cy = face.cy;

        cell.faceEnable(face.type, false);
        face.setPerk(this);
        rotation = Face.SIDE_ANGLE[face.type];

        Cell connected = cell.connected[faceIndex];
        int ftype = Face.SIDE_CONNECTOR[face.type];
        connected.faceEnable(ftype, false);
        Face conFace = connected.faces[ftype/2];
        conFace.setPerk(this);
    }

    @Override
    public int getType()
    {
        return PERK_BARRIER;
    }

    @Override
    public boolean applyBonus(Engine game, Cell cell1)
    {
        return false;
    }

    @Override
    public boolean applyOnSet(Engine game, Cell cell)
    {
        return false;
    }
}
