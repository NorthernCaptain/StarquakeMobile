package northern.captain.quadronia.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import northern.captain.quadronia.game.BoundingBox;
import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Element;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.tools.Log;

/**
 * Created by leo on 3/10/15.
 */
public class FieldElementDraw
{
    Map<Integer, Sprite> images = new HashMap<Integer, Sprite>();
    Field field;
    static SpriteCache cache;
    int cacheIdx;
    boolean cacheInited = false;

    public FieldElementDraw(Field field)
    {
        this.field = field;
    }

    public void initGraphics(XMLLayoutLoader loader, TextureAtlas atlas)
    {
        Sprite sprite;
        sprite = loader.newSprite("itemSESW", atlas);
        images.put(Face.getPair(Face.FC_SE, Face.FC_SW), sprite);
        images.put(Face.getPair(Face.FC_SW, Face.FC_SE), sprite);

        sprite = loader.newSprite("itemNENW", atlas);
        images.put(Face.getPair(Face.FC_NE, Face.FC_NW), sprite);
        images.put(Face.getPair(Face.FC_NW, Face.FC_NE), sprite);

        sprite = loader.newSprite("itemNSW", atlas);
        images.put(Face.getPair(Face.FC_NORTH, Face.FC_SW), sprite);
        images.put(Face.getPair(Face.FC_SW, Face.FC_NORTH), sprite);

        sprite = loader.newSprite("itemNSE", atlas);
        images.put(Face.getPair(Face.FC_NORTH, Face.FC_SE), sprite);
        images.put(Face.getPair(Face.FC_SE, Face.FC_NORTH), sprite);

        sprite = loader.newSprite("itemSNW", atlas);
        images.put(Face.getPair(Face.FC_SOUTH, Face.FC_NW), sprite);
        images.put(Face.getPair(Face.FC_NW, Face.FC_SOUTH), sprite);

        sprite = loader.newSprite("itemSNE", atlas);
        images.put(Face.getPair(Face.FC_SOUTH, Face.FC_NE), sprite);
        images.put(Face.getPair(Face.FC_NE, Face.FC_SOUTH), sprite);

        if(cache != null)
        {
            cache.dispose();
            cache = null;
        }

        cache = new SpriteCache(1000, true);
        cache.setProjectionMatrix(NContext.current.camera.projection);
        cache.setTransformMatrix(NContext.current.camera.view);
        cacheInited = false;
    }

    public void drawCache(int ox, int oy)
    {
        Set<Cell> cells = field.getAllocatedCells();

        cache.clear();
        cache.beginCache();

        for(Cell cell : cells)
        {
            if(cell.hasElement())
            {
                Sprite sprite = images.get(cell.getElement().mask);
                try
                {
                    if(cell.hasPerk())
                    {
                        cache.setColor(1.f, 0.f, 0.7f, 0.5f);
                        cache.add(sprite, ox + cell.eleX, oy - cell.eleY);
                        cache.setColor(1, 1, 1, 1);
                    } else
                    {
                        cache.add(sprite, ox + cell.eleX, oy - cell.eleY);
                    }
                }
                catch(Exception ex)
                {
                    cache.setColor(1, 1, 1, 1);
                    Element element = cell.getElement();
                    Log.e("curveIt", "ERROR element draw: " + element.mask + ": " + element.sides[Element.ONE]
                            + ", " + element.sides[Element.TWO]);
                }
            }
        }

        cacheIdx = cache.endCache();
        field.resetHasCellsChanged();
        cacheInited = true;
    }


    public void draw(Batch batch, float parentAlpha, int ox, int oy)
    {
        Set<Cell> cells = field.getAllocatedCells();
        if(cells.isEmpty()) return;

        if(field.isHasCellsChanged() || !cacheInited)
        {
            drawCache(ox, oy);
        }

//        for(Cell cell : cells)
//        {
//            if(cell.hasElement())
//            {
//                Sprite sprite = images.get(cell.getElement().mask);
//                try
//                {
//                    if(cell.hasPerk())
//                    {
//                        batch.setColor(1.f, 0.f, 0.7f, 0.5f);
//                        batch.draw(sprite, ox + cell.eleX, oy - cell.eleY);
//                        batch.setColor(1, 1, 1, 1);
//                    } else
//                    {
//                        batch.draw(sprite, ox + cell.eleX, oy - cell.eleY);
//                    }
//                }
//                catch(Exception ex)
//                {
//                    batch.setColor(1, 1, 1, 1);
//                    Element element = cell.getElement();
//                    Log.e("curveIt", "ERROR element draw: " + element.mask + ": " + element.sides[Element.ONE]
//                            + ", " + element.sides[Element.TWO]);
//                }
//            }
//        }
        batch.end();
        cache.begin();
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        cache.draw(cacheIdx);
        cache.end();
        batch.begin();
    }

    public void drawZoom(Batch batch, float parentAlpha, int ox, int oy, BoundingBox box)
    {
        Set<Cell> cells = field.getAllocatedCells();
        for(Cell cell : cells)
        {
            if(cell.hasElement() && box.intersect(cell))
            {
                Sprite sprite = images.get(cell.getElement().mask);
                try
                {
                    batch.draw(sprite, ox + cell.eleX, oy - cell.eleY);
                }
                catch(Exception ex)
                {
                    Element element = cell.getElement();
                    Log.e("curveIt", "ERROR element draw: " + element.mask + ": " + element.sides[Element.ONE]
                            + ", " + element.sides[Element.TWO]);
                }
            }
        }
    }

    public void onOrientationChange()
    {
        cache.setProjectionMatrix(NContext.current.camera.projection);
        cache.setTransformMatrix(NContext.current.camera.view);
    }

    public void onFieldRebuild()
    {
        cache.clear();
        cacheInited = false;
    }
}
