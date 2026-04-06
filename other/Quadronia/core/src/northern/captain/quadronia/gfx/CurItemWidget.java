package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.ItemFactory;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLContentLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 10.03.15.
 */
public class CurItemWidget extends Widget
{
    Sprite[] images;
    Engine game;
    String name;

    int width;
    int height;

    int index;
    int mode;

    float alpha = 1;

    public CurItemWidget(String name, Engine game, int index, int mode)
    {
        this.name = name;
        this.game = game;
        this.index = index;
        this.mode = mode;
    }

    public void initGraphics(XMLLayoutLoader loader, TextureAtlas atlas)
    {
        String[] names = ItemFactory.instance.getItemNames(ItemFactory.MODE_ALL);

        Table tbl = loader.newTable(name, atlas);
        setPosition(tbl.getX(), tbl.getY());
        width = (int)tbl.getWidth();
        height = (int)tbl.getHeight();

        images = new Sprite[names.length];

        for(int i = 0; i<images.length;i++)
        {
            images[i] = loader.newSprite(
                    index != 0 ? names[i] + "_n1" : names[i], atlas);
            images[i].setOriginCenter();
            images[i].setPosition(getX() - images[i].getOriginX(), getY() - images[i].getOriginY());
        }

    }

    @Override
    public float getMinWidth () {
        return width;
    }

    @Override
    public float getMinHeight () {
        return height;
    }

    @Override
    public float getPrefWidth () {
        return width;
    }

    @Override
    public float getPrefHeight () {
        return height;
    }

    @Override
    public float getMaxWidth () {
        return width;
    }

    @Override
    public float getMaxHeight () {
        return height;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);
        Sprite sprite = images[game.currentItem[index].idx];
        sprite.draw(batch, parentAlpha);
    }

    public Sprite getSprite(int index)
    {
        return images[game.currentItem[index].idx];
    }
}
