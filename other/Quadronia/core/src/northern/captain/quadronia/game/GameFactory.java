package northern.captain.quadronia.game;

/**
 * Created by leo on 05.03.15.
 */
public class GameFactory
{
    public static GameFactory instance = new GameFactory();

    public Field newField(int sizeX, int sizeY) { return new FieldHoriz(sizeX, sizeY);}

    public Cell newCell(int cx, int cy, Field field) { return new CellHoriz(cx, cy, field);}
}
