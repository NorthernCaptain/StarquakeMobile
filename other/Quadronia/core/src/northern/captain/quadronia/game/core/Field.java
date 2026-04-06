package northern.captain.quadronia.game.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import northern.captain.quadronia.game.BoundingBox;
import northern.captain.quadronia.game.Point;
import northern.captain.tools.IDisposable;
import northern.captain.tools.IJSONSerializer;

/**
 * Created by leo on 30.08.15.
 */
public class Field implements IDisposable, IJSONSerializer
{
    public int width;
    public int height;

    public Quad[][] quads;
    public int maxColors = 4;

    public Field(int w, int h)
    {
        width = w;
        height = h;

        quads = new Quad[width][height];
        for(int x=0;x<width;x++)
        {
            quads[x] = new Quad[height];
        }
    }

    public void initEmptyField()
    {
        for(int y=0;y<height;y++)
        {
            for(int x = 0; x< width;x++)
            {
                Quad quad = Game.QUAD_POOL.obtain();
                quads[x][y] = quad;
                quad.init(x,y);
            }
        }
    }

    public void initField(int maxColors, IColorGenerator generator)
    {
        generator.init();
        this.maxColors = maxColors;
        for(int y=0;y<height;y++)
        {
            for(int x = 0; x< width;x++)
            {
                Quad quad = Game.QUAD_POOL.obtain();
                quads[x][y] = quad;
                quad.init(x,y);
                quad.setType(generator.generate(maxColors, x, y, width, height));
            }
        }
    }

    public void initFieldArea(QuadArea area, IColorGenerator generator)
    {
        if(area.len == width && area.hei == height)
        {
            initField(maxColors, generator);
            return;
        }

        generator.init();
        for(int y=0;y<area.hei;y++)
        {
            int fy = area.fromY + y;
            for(int x = 0; x< area.len;x++)
            {
                int fx = area.fromX + x;

                Quad quad1 = quads[fx][fy];
                if(quad1 != null && quad1.type == Quad.TYPE_NO) continue;

                Quad quad = Game.QUAD_POOL.obtain();
                quads[fx][fy] = quad;
                quad.init(fx,fy);
                quad.setType(generator.generate(maxColors, fx, fy, width, height));
            }
        }
    }

    @Override
    public void dispose()
    {
        for(int y=0;y<height;y++)
        {
            for(int x = 0; x< width;x++)
            {
                Quad quad = quads[x][y];
                if(quad != null)
                {
                    Game.QUAD_POOL.free(quad);
                    quads[x][y] = null;
                }
            }
        }
    }

    public Config getConfig() { return null;}
    public Quad getQuadByCoord(int screenX, int screenY)
    {
        return null;
    }

    public void getQuadCoordByXY(int x, int y, BoundingBox box)
    {
    }

    public void getQuadCenterByXY(int x, int y, Point point)
    {
    }

    public QuadArea subArray(int fromX, int fromY, int len, int hei)
    {
        QuadArea area = new QuadArea(fromX, fromY, len, hei);
        Quad sub[][] = new Quad[len][hei];
        for(int x=0;x<len;x++)
        {
            sub[x] = new Quad[hei];
            for(int y = 0;y<hei;y++)
            {
                sub[x][y] = quads[x+fromX][y+fromY];
            }
        }

        area.quads = sub;
        return area;
    }

    /**
     * Deserialize object from the given JSONObject. The given object is not a container,
     * the object that really contains the data for deserialization
     *
     * @param jobj - data for the field and quads
     */
    @Override
    public void deserializeJSON(JSONObject jobj)
    {
        dispose();
        try
        {
            JSONArray jar = jobj.getJSONArray("qd");
            int idx = 0;
            for(int x=0;x<width;x++)
            {
                for (int y = 0; y < height; y++)
                {
                    Quad quad = Game.QUAD_POOL.obtain();
                    quads[x][y] = quad;
                    quad.init(x,y);
                    quad.type = jar.getInt(idx++);
                    quad.subtype = jar.getInt(idx++);
                }
            }

        } catch (Exception jex) {}
    }

    /**
     * Serialize object into the JSONObject. Object should create a new JSONObject,
     * put all data into it and then return this json to the caller.
     */
    @Override
    public JSONObject serializeJSON()
    {
        JSONObject jobj = new JSONObject();
        try
        {
            jobj.put("w", width);
            jobj.put("h", height);
            JSONArray jar = new JSONArray();

            for(int x=0;x<width;x++)
            {
                for (int y = 0; y < height; y++)
                {
                    Quad quad = quads[x][y];
                    jar.put(quad.type);
                    jar.put(quad.subtype);
                }
            }
            jobj.put("qd", jar);
        }
        catch (JSONException jex) {}
        return jobj;
    }
}
