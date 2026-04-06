package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 02.08.13
 * Time: 23:13
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class IndexImage extends Widget
{
    private Drawable[] index;
    private IntHolder indexer;
    public IndexImage(Drawable[] indexes, IntHolder indexer)
    {
        this(indexes[0]);
        this.index = indexes;
        this.indexer = indexer;
    }

    public void setIndex(Drawable[] indexes, IntHolder indexer)
    {
        this.index = indexes;
        this.indexer = indexer;
    }

    @Override
    public void draw (Batch batch, float parentAlpha)
    {
        validate();

        int idx = indexer.value;
        if(idx < 0 || idx >= index.length)
            return;

        Drawable drawable = index[idx];

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        float x = getX();
        float y = getY();
        float scaleX = getScaleX();
        float scaleY = getScaleY();

        if (drawable != null) {
            if (drawable.getClass() == TextureRegionDrawable.class) {
                TextureRegion region = ((TextureRegionDrawable)drawable).getRegion();
                float rotation = getRotation();
                if (scaleX == 1 && scaleY == 1 && rotation == 0)
                    batch.draw(region, x + imageX, y + imageY, imageWidth, imageHeight);
                else {
                    batch.draw(region, x + imageX, y + imageY, getOriginX() - imageX, getOriginY() - imageY, imageWidth, imageHeight,
                            scaleX, scaleY, rotation);
                }
            } else
                drawable.draw(batch, x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
        }
    }

    private Scaling scaling;
    private int align = Align.center;
    private float imageX, imageY, imageWidth, imageHeight;
    private Drawable drawable;


    /** Creates an image stretched, and aligned center.
     * @param drawable May be null. */
    public IndexImage (Drawable drawable) {
        this(drawable, Scaling.stretch, Align.center);
    }

        /** Creates an image aligned center.
         * @param drawable May be null. */
    public IndexImage (Drawable drawable, Scaling scaling) {
        this(drawable, scaling, Align.center);
    }

        /** @param drawable May be null. */
    public IndexImage (Drawable drawable, Scaling scaling, int align) {
        setDrawable(drawable);
        this.scaling = scaling;
        this.align = align;
        setWidth(getPrefWidth());
        setHeight(getPrefHeight());
    }

    public void layout () {
        if (drawable == null) return;

        float regionWidth = drawable.getMinWidth();
        float regionHeight = drawable.getMinHeight();
        float width = getWidth();
        float height = getHeight();

        Vector2 size = scaling.apply(regionWidth, regionHeight, width, height);
        imageWidth = size.x;
        imageHeight = size.y;

        if ((align & Align.left) != 0)
            imageX = 0;
        else if ((align & Align.right) != 0)
            imageX = (int)(width - imageWidth);
        else
            imageX = (int)(width / 2 - imageWidth / 2);

        if ((align & Align.top) != 0)
            imageY = (int)(height - imageHeight);
        else if ((align & Align.bottom) != 0)
            imageY = 0;
        else
            imageY = (int)(height / 2 - imageHeight / 2);
    }

    public void setDrawable (Drawable drawable) {
        if (drawable != null) {
            if (this.drawable == drawable) return;
            if (getPrefWidth() != drawable.getMinWidth() || getPrefHeight() != drawable.getMinHeight()) invalidateHierarchy();
        } else {
            if (getPrefWidth() != 0 || getPrefHeight() != 0) invalidateHierarchy();
        }
        this.drawable = drawable;
    }

    public Drawable getDrawable () {
        return drawable;
    }

    public void setScaling (Scaling scaling) {
        if (scaling == null) throw new IllegalArgumentException("scaling cannot be null.");
        this.scaling = scaling;
    }

    public void setAlign (int align) {
        this.align = align;
    }

    public float getMinWidth () {
        return 0;
    }

    public float getMinHeight () {
        return 0;
    }

    public float getPrefWidth () {
        if (drawable != null) return drawable.getMinWidth();
        return 0;
    }

    public float getPrefHeight () {
        if (drawable != null) return drawable.getMinHeight();
        return 0;
    }

    public float getImageX () {
        return imageX;
    }

    public float getImageY () {
        return imageY;
    }

    public float getImageWidth () {
        return imageWidth;
    }

    public float getImageHeight () {
        return imageHeight;
    }

}
