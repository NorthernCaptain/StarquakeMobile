package northern.captain.quadronia.game.core;

import northern.captain.gamecore.glx.NContext;
import northern.captain.quadronia.game.BoundingBox;
import northern.captain.quadronia.game.Point;

/**
 * Created by leo on 30.08.15.
 */
public class GField extends Field
{
    public Config config;
    public GField(Config config)
    {
        super(config.fieldWidthPix/config.quadOuterWidthPix, config.fieldHeightPix/config.quadOuterHeightPix);
        this.config = config;
    }

    @Override
    public Quad getQuadByCoord(int screenX, int screenY)
    {
        int cx = (screenX - config.screenDeltaXPix)/config.quadOuterWidthPix;

        if(cx < 0 || cx >= width) return null;

        int cy = ((NContext.current.screenHeight - screenY) - config.screenDeltaYPix)/config.quadOuterHeightPix;

        if(cy < 0 || cy >= height) return null;

        return quads[cx][cy];
    }

    @Override
    public void getQuadCoordByXY(int x, int y, BoundingBox box)
    {
        box.xBox1 = config.screenDeltaXPix + x * config.quadOuterWidthPix;
        box.yBox2 = NContext.current.screenHeight - (config.screenDeltaYPix + y * config.quadOuterHeightPix);
        box.xBox2 = box.xBox1 + config.quadOuterWidthPix;
        box.yBox1 = box.yBox2 - config.quadOuterHeightPix;
    }

    @Override
    public void getQuadCenterByXY(int x, int y, Point point)
    {
        point.x = config.screenDeltaXPix + x * config.quadOuterWidthPix + config.quadOuterHalfWidth;
        point.y = NContext.current.screenHeight - (config.screenDeltaYPix + y * config.quadOuterHeightPix + config.quadOuterHalfHeight);

    }

    @Override
    public Config getConfig()
    {
        return config;
    }
}
