package northern.captain.quadronia.game;

/**
 * Created by leo on 01.03.15.
 */

public class GeoFace extends Face
{
    public int x1, x2;
    public int y1, y2;

    public int cx, cy;

    public GeoFace(int type)
    {
        super(type);
    }

    public GeoFace setXY(int x1, int y1, int x2, int y2)
    {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;

        cx = (int) Math.floor((x1 + x2)/2);
        cy = (int) Math.floor((y1 + y2)/2);

        return this;
    }

    @Override
    public GeoFace setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        return this;
    }
}
