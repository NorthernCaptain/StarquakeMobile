package northern.captain.quadronia.game;

/**
 * Created by leo on 3/2/15.
 */
public class BoundingBox
{
    public int xBox1;
    public int yBox1;
    public int xBox2;
    public int yBox2;

    public boolean isIn(int x, int y)
    {
        return x>=xBox1 && x<xBox2 && y>=yBox1 && y<yBox2;
    }

    public boolean isY(int y) { return y>=yBox1 && y<yBox2;}
    public boolean isX(int x) { return x>=xBox1 && x<xBox2;}

    public boolean intersect(BoundingBox box2)
    {
        return !(box2.xBox2 < xBox1 || box2.xBox1 > xBox2
                || box2.yBox2 < yBox1 || box2.yBox1 > yBox2);
    }

    public boolean intersectY(BoundingBox box2)
    {
        return !(box2.yBox2 < yBox1 || box2.yBox1 > yBox2);
    }

    public BoundingBox() {}

    public BoundingBox(int x1, int y1, int x2, int y2)
    {
        xBox1 = x1;
        xBox2 = x2;
        yBox1 = y1;
        yBox2 = y2;
    }
}
