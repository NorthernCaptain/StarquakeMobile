package northern.captain.quadronia.game.core;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLContentLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 30.08.15.
 */
public class Config
{
    public int fieldDeltaXPix;
    public int fieldDeltaYPix;
    /**
     * Field width in pixels
     */
    public int fieldWidthPix;
    /**
     * Field height in pixels
     */
    public int fieldHeightPix;

    /**
     * Quad outer width in pixels (quad + it's border)
     */

    public int quadOuterWidthPix;

    /**
     * Quad outer half width in pixels
     */
    public int quadOuterHalfWidth;
    /**
     * Quad outer height in pixels (quad + it's border)
     */
    public int quadOuterHeightPix;

    /**
     * Quad outer half height in pixels
     */
    public int quadOuterHalfHeight;

    /**
     * delta X offset in pixels for upper left quad
     */
    public int screenDeltaXPix;
    /**
     * delta Y offset in pixels for upper left quad
     */
    public int screenDeltaYPix;

    /**
     * Number of pixels we shift from the outer left corner to draw the selected quad
     */
    public int selectedDeltaPix;

    public void init()
    {
        int fieldW = fieldWidthPix / quadOuterWidthPix;
        int fieldH = fieldHeightPix / quadOuterHeightPix;

        screenDeltaXPix = fieldDeltaXPix + (fieldWidthPix - fieldW * quadOuterWidthPix)/2;
        screenDeltaYPix = fieldDeltaYPix + (fieldHeightPix - fieldH * quadOuterHeightPix)/2;

        quadOuterHalfWidth = quadOuterWidthPix/2;
        quadOuterHalfHeight = quadOuterHeightPix/2;
    }

    public Config(XMLLayoutLoader loader)
    {
        XMLContentLoader.Node node = loader.getNode("fldcfg");
        quadOuterWidthPix = NContext.current.iScale(loader.getAttribInt(node, "quadW", 1));
        quadOuterHeightPix = NContext.current.iScale(loader.getAttribInt(node, "quadH", 1));

        fieldWidthPix = NContext.current.iScale(loader.getAttribInt(node, "areaW", 1));
        int topOffset = NContext.current.iScale(loader.getAttribInt(node, "topY", 1));
        int bottomOffset = NContext.current.iScale(loader.getAttribInt(node, "botY", 1));
        selectedDeltaPix = NContext.current.iScale(loader.getAttribInt(node, "selectD", 1));

        fieldDeltaXPix = (NContext.current.screenWidth - fieldWidthPix)/2;
        fieldDeltaYPix = topOffset + NContext.current.gameAreaDeltaY;

        fieldHeightPix = NContext.current.gameAreaHeight - topOffset - bottomOffset;

        init();
    }
}
