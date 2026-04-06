package northern.captain.quadronia.gfx.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.squareup.otto.Subscribe;

import java.util.Arrays;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.BoundingBox;
import northern.captain.quadronia.game.Constants;
import northern.captain.quadronia.game.Point;
import northern.captain.quadronia.game.core.Config;
import northern.captain.quadronia.game.core.Field;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.core.Quad;
import northern.captain.quadronia.game.core.QuadArea;
import northern.captain.quadronia.game.core.QuadCollector;
import northern.captain.quadronia.game.events.EFieldChanged;
import northern.captain.quadronia.game.events.EFullReshuffle;
import northern.captain.quadronia.game.events.EGameLevelUp;
import northern.captain.quadronia.game.events.EPerkDoBomb;
import northern.captain.quadronia.game.events.EPerkShowHint;
import northern.captain.quadronia.game.events.EQuadAreaHitFinish;
import northern.captain.quadronia.game.events.EQuadAreaHitMiddle;
import northern.captain.quadronia.game.events.EQuadAreaHitStart;
import northern.captain.quadronia.game.events.EQuadSelect;
import northern.captain.quadronia.game.events.ESolutionFound;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IAction;
import northern.captain.quadronia.gfx.IGraphicsInit;
import northern.captain.tools.Helpers;
import northern.captain.tools.IDisposable;
import northern.captain.tools.Log;

/**
 * Created by leo on 01.09.15.
 */
public class QuadsDraw implements IGraphicsInit, IDisposable, IAction
{
    private Sprite[] colorSprites = new Sprite[Constants.MAX_COLORS*5 + 6];

    private Sprite[][] quads;

    private Field field;
    private Game game;
    private Config config;

    private Sprite hLines[];
    private Sprite hLine;

    private Sprite vLines[];
    private Sprite vLine;

    private Sprite shadow;
    private Sprite background;

    private Point point = new Point();
    private BoundingBox box = new BoundingBox();

    private Array<BitAnimQ> selected = new Array<BitAnimQ>(10);

    private Array<IQuadAreaAnim> areaAnims = new Array<IQuadAreaAnim>(false, 4);

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        String theme = "" + NCore.instance().getGameOptionsMenu().getThemeIndex();
        for(int i=1;i<Constants.MAX_COLORS;i++)
        {
            colorSprites[i] = loader.newSprite(theme + "col" + i, gContext.atlas);
        }
        colorSprites[Constants.MAX_COLORS] = loader.newSprite(theme+"colmulti", gContext.atlas);
        colorSprites[Constants.MAX_COLORS + 1] = loader.newSprite(theme+"colx2", gContext.atlas);
        colorSprites[Constants.MAX_COLORS + 2] = loader.newSprite(theme+"colm200", gContext.atlas);
        colorSprites[Constants.MAX_COLORS + 3] = loader.newSprite(theme+"colno", gContext.atlas);
        colorSprites[Constants.MAX_COLORS + 4] = loader.newSprite(theme+"colcoin", gContext.atlas);
        colorSprites[Constants.MAX_COLORS + 5] = loader.newSprite(theme+"coltime", gContext.atlas);

        int delta = Constants.MAX_COLORS*2;
        colorSprites[delta + Quad.TYPE_BLUE] = loader.newSprite(theme+"bicol" + Quad.TYPE_BLUE, gContext.atlas);
        colorSprites[delta + Quad.TYPE_RED] = loader.newSprite(theme+"bicol" + Quad.TYPE_RED, gContext.atlas);
        colorSprites[delta + Quad.TYPE_AMBER] = loader.newSprite(theme+"bicol" + Quad.TYPE_AMBER, gContext.atlas);
        colorSprites[delta + Quad.TYPE_PURPLE] = loader.newSprite(theme+"bicol" + Quad.TYPE_PURPLE, gContext.atlas);
        colorSprites[delta + Quad.TYPE_GREEN] = loader.newSprite(theme+"bicol" + Quad.TYPE_GREEN, gContext.atlas);
        colorSprites[delta + Quad.TYPE_CYAN] = loader.newSprite(theme+"bicol" + Quad.TYPE_CYAN, gContext.atlas);
        colorSprites[delta + Quad.TYPE_BLUEGRAY] = loader.newSprite(theme+"bicol" + Quad.TYPE_BLUEGRAY, gContext.atlas);
        colorSprites[delta + Quad.TYPE_PINK] = loader.newSprite(theme+"bicol" + Quad.TYPE_PINK, gContext.atlas);

        delta = Constants.MAX_COLORS*3;
        colorSprites[delta + Quad.TYPE_BLUE] = loader.newSprite(theme+"colx2" + Quad.TYPE_BLUE, gContext.atlas);
        colorSprites[delta + Quad.TYPE_LIME] = loader.newSprite(theme+"colx2" + Quad.TYPE_LIME, gContext.atlas);
        colorSprites[delta + Quad.TYPE_GREEN] = loader.newSprite(theme+"colx2" + Quad.TYPE_GREEN, gContext.atlas);
        colorSprites[delta + Quad.TYPE_ORANGE] = loader.newSprite(theme+"colx2" + Quad.TYPE_ORANGE, gContext.atlas);
        colorSprites[delta + Quad.TYPE_PURPLE] = loader.newSprite(theme+"colx2" + Quad.TYPE_PURPLE, gContext.atlas);
        colorSprites[delta + Quad.TYPE_RED] = loader.newSprite(theme+"colx2" + Quad.TYPE_RED, gContext.atlas);

        delta = Constants.MAX_COLORS*4;
        // TYPE_MULTI
        colorSprites[delta + 0] = loader.newSprite(theme+"colmulti0", gContext.atlas);
        colorSprites[delta + 1] = loader.newSprite(theme+"colmulti1", gContext.atlas);
        colorSprites[delta + 2] = loader.newSprite(theme+"colmulti2", gContext.atlas);
        colorSprites[delta + 3] = loader.newSprite(theme+"colmulti3", gContext.atlas);


        hLine = loader.newSprite("hline", gContext.atlas);
        vLine = loader.newSprite("vline", gContext.atlas);
        shadow = loader.newSprite("qshadow", gContext.atlas);
        background = loader.newSprite("fldback", gContext.atlas);

        lineStretch1.initGraphics(loader, gContext.atlas);
        lineStretch2.initGraphics(loader, gContext.atlas);
        lineStretch3.initGraphics(loader, gContext.atlas);
        lineStretch4.initGraphics(loader, gContext.atlas);

        selected.clear();
        areaAnims.clear();
        hitAnim.load(loader, gContext);
        hitAnim.clear();
    }

    public void initField()
    {
        if(quads == null)
        {
            quads = new Sprite[field.width][field.height];
            for(int x = 0; x< field.width; x++)
            {
                quads[x] = new Sprite[field.height];
            }

            hLines = new Sprite[field.height+1];
            vLines = new Sprite[field.width+1];
        }

        initFieldArea(0, 0, field.width, field.height);

        int screenHeight = NContext.current.screenHeight;

        background.setBounds(config.fieldDeltaXPix,
                screenHeight - config.fieldDeltaYPix - config.fieldHeightPix,
                config.fieldWidthPix, config.fieldHeightPix);
        background.setAlpha(0.8f);

        int h2 = ((int)hLine.getHeight())/2;
        for(int i=0;i<hLines.length;i++)
        {
            Sprite sprite = NCore.SPRITE_POOL.obtain();
            sprite.set(hLine);
            sprite.setBounds(config.fieldDeltaXPix,
                screenHeight - (config.screenDeltaYPix + i * config.quadOuterHeightPix),
                config.fieldWidthPix, sprite.getHeight());
            sprite.setColor(1, 1, 1, 0.8f);
            sprite.setRotation(0);
            hLines[i] = sprite;
        }

        int w2 = ((int)vLine.getWidth())/2;
        for(int i=0;i<vLines.length;i++)
        {
            Sprite sprite = NCore.SPRITE_POOL.obtain();
            sprite.set(vLine);
            sprite.setBounds(config.screenDeltaXPix + i * config.quadOuterWidthPix,
                screenHeight - config.fieldDeltaYPix - config.fieldHeightPix,
                sprite.getWidth(), config.fieldHeightPix);
            sprite.setAlpha(0.8f);
            sprite.setRotation(0);
            vLines[i] = sprite;
        }

        QuadAppearAnim anim = new QuadAppearAnim();
        anim.prepare(0, 0, field.width, field.height);
        NCore.instance().getSoundman().playSound(ISoundMan.SND_QUAD_AREA_APPEAR, false);

        areaAnims.add(anim);

        NCore.busRegister(this);
    }

    void initFieldArea(int fromX, int fromY, int len, int hei)
    {
        Point point = new Point();
        int spriteW2 = (int)colorSprites[1].getWidth()/2;
        int spriteH2 = (int)colorSprites[1].getHeight()/2;
        int deltaColor = Constants.MAX_COLORS2;
        int type;

        for(int x = 0; x< len; x++)
        {
            int fx = x  + fromX;
            for(int y = 0;y < hei;y++)
            {
                int fy = fromY + y;
                Sprite sprite = NCore.SPRITE_POOL.obtain();
                quads[fx][fy] = sprite;
                type = field.quads[fx][fy].type;
                if(type < 0) type = -type + deltaColor;
                if(type == Quad.TYPE_X2) type = Constants.MAX_COLORS3 + field.quads[fx][fy].subtype;
                if(type == Quad.TYPE_MULTI) type = Constants.MAX_COLORS4 + field.quads[fx][fy].subtype;
                sprite.set(colorSprites[type]);
                field.getQuadCenterByXY(fx, fy, point);
                sprite.setPosition(point.x - spriteW2, point.y - spriteH2);
                sprite.setAlpha(1f);
                sprite.setRotation((0.5f - Helpers.RND.nextFloat()) * 15);
                sprite.setScale(1);
            }
        }
    }

    private Sprite newFieldSprite(int fx, int fy)
    {
        int spriteW2 = (int)colorSprites[1].getWidth()/2;
        int spriteH2 = (int)colorSprites[1].getHeight()/2;
        int deltaColor = Constants.MAX_COLORS2;
        Sprite sprite = NCore.SPRITE_POOL.obtain();
        quads[fx][fy] = sprite;
        int type = field.quads[fx][fy].type;
        if(type < 0) type = -type + deltaColor;
        if(type == Quad.TYPE_X2) type = Constants.MAX_COLORS3 + field.quads[fx][fy].subtype;
        if(type == Quad.TYPE_MULTI) type = Constants.MAX_COLORS4 + field.quads[fx][fy].subtype;
        sprite.set(colorSprites[type]);
        field.getQuadCenterByXY(fx, fy, point);
        sprite.setPosition(point.x - spriteW2, point.y - spriteH2);
        sprite.setAlpha(1f);
        sprite.setRotation((0.5f - Helpers.RND.nextFloat())*15);
        return sprite;
    }

    public void clearField(Sprite[][] quads)
    {
        if(quads == null) return;
        for(int x = 0; x< quads.length; x++)
        {
            for(int y = 0;y < quads[x].length;y++)
            {
                Sprite sprite = quads[x][y];
                if(sprite != null)
                {
                    NCore.SPRITE_POOL.free(sprite);
                    quads[x][y] = null;
                }
            }
        }
    }

    public void setGame(Game game)
    {
        field = game.getField();
        this.game = game;
        this.config = field.getConfig();
        clearField(quads);
        initField();
    }

    public void drawBG(Batch batch, float parentAlpha)
    {
        background.draw(batch, parentAlpha);

        for (int i = 0; i < hLines.length; i++)
        {
            hLines[i].draw(batch, parentAlpha);
        }

        for (int i = 0; i < vLines.length; i++)
        {
            vLines[i].draw(batch, parentAlpha);
        }

        lineStretch1.draw(batch);
        lineStretch2.draw(batch);
        lineStretch3.draw(batch);
        lineStretch4.draw(batch);
    }

    public void draw(Batch batch, float parentAlpha)
    {
        if(game.isPaused()) return;

        Sprite quad;
        for(int x = 0; x< field.width; x++)
        {
            for (int y = 0; y < field.height; y++)
            {
                quad = quads[x][y];
                if(quad != null)
                {
                    quad.draw(batch, parentAlpha);
                }
            }
        }

        for(BitAnimQ animQ: selected)
        {
            animQ.shadowSprite.draw(batch, parentAlpha);
        }

        for(BitAnimQ animQ: selected)
        {
            animQ.quadSprite.draw(batch, parentAlpha);
        }

        for(IQuadAreaAnim areaAnim : areaAnims)
        {
            areaAnim.draw(batch);
        }

        hitAnim.draw(batch);
    }

    @Override
    public void dispose()
    {
        clearField(quads);
        for(IQuadAreaAnim anim : areaAnims)
        {
            anim.clearArea(true);
        }
        areaAnims.clear();
        NCore.busUnregister(this);
    }

    @Override
    public void act(float delta)
    {
        for(int i = 0;i<areaAnims.size;i++)
        {
            IQuadAreaAnim anim = areaAnims.get(i);

            if(!anim.act(delta))
            {
                areaAnims.removeIndex(i);
                i--;
            }
        }
        lineStretch1.act(delta);
        lineStretch2.act(delta);
        lineStretch3.act(delta);
        lineStretch4.act(delta);
        hitAnim.act(delta);
    }

    static class BitAnimQ implements Pool.Poolable
    {
        float origX;
        float origY;
        float origCenterX;
        float origCenterY;

        int origCx;
        int origCy;
        int quadType;

        Sprite quadSprite;
        Sprite shadowSprite;

        BitAnimQ set(Sprite sprite)
        {
            quadSprite = sprite;
            if(sprite != null)
            {
                origX = sprite.getX();
                origY = sprite.getY();
                origCenterX = origX + sprite.getWidth() / 2;
                origCenterY = origY + sprite.getHeight() / 2;
            }
            return this;
        }

        void setPosition(float deltaX, float deltaY)
        {
            quadSprite.setPosition(origX + deltaX, origY + deltaY);
        }

        void restoreOrig()
        {
            quadSprite.setScale(1);
            quadSprite.setAlpha(1);
            quadSprite.setPosition(origX, origY);
        }

        void dispose()
        {
            if(quadSprite != null)
            {
                NCore.SPRITE_POOL.free(quadSprite);
                quadSprite = null;
            }

            if(shadowSprite != null)
            {
                NCore.SPRITE_POOL.free(shadowSprite);
                shadowSprite = null;
            }
        }

        /**
         * Resets the object for reuse. Object references should be nulled and fields may be set to default values.
         */
        @Override
        public void reset()
        {
            quadSprite = shadowSprite = null;
        }
    }

    private static final Pool<BitAnimQ> BIT_ANIM_Q_POOL = new Pool<BitAnimQ>()
    {
        @Override
        protected BitAnimQ newObject()
        {
            return new BitAnimQ();
        }
    };

    interface IQuadAreaAnim
    {
        void clearArea(boolean dispose);
        void prepare(int x, int y, int len, int hei);
        boolean act(float delta);
        void draw(Batch batch);
    }

    abstract class QuadAreaAnim implements IQuadAreaAnim
    {
        boolean inAnimation = false;

        Interpolation moveInter = Interpolation.pow4In;
        int moveDeltaX;
        int moveDeltaY;

        int moveX, moveY, moveLen, moveHei;
        float moveTime;
        float moveTotalTime = 0.6f;
        float moveTotalDelay = 0.8f;
        float moveDelay;

        BitAnimQ[][] area;

        public void clearArea(boolean dispose)
        {
            if(area == null) return;

            for(int x = 0;x< area.length;x++)
            {
                for(int y = 0; y< area[x].length;y++)
                {
                    BitAnimQ animQ = area[x][y];
                    if(animQ != null)
                    {
                        if(animQ.quadSprite != null)
                        {
                            animQ.restoreOrig();
                            quads[animQ.origCx][animQ.origCy] = animQ.quadSprite;
                        }

                        if(dispose) animQ.dispose();
                        BIT_ANIM_Q_POOL.free(animQ);
                        area[x][y] = null;
                    }
                }
            }
        }

        public void prepare(int x, int y, int len, int hei)
        {
            moveDeltaX = NContext.current.screenWidth;
            moveDeltaY = NContext.current.screenHeight;
            moveX = x;
            moveY = y;
            moveLen = len;
            moveHei = hei;

            clearArea(false);
            area = new BitAnimQ[len][hei];
            float apply = moveInter.apply(1);
            for(int ix = 0;ix < len;ix++)
            {
                area[ix] = new BitAnimQ[hei];
                for(int iy=0;iy<hei;iy++)
                {
                    BitAnimQ animQ = BIT_ANIM_Q_POOL.obtain();
                    area[ix][iy]=animQ;
                    animQ.origCx = x + ix;
                    animQ.origCy = y + iy;
                    prepareAnim(animQ, x, y, ix, iy, apply);
                    if(animQ.quadSprite == null)
                    {
                        area[ix][iy] = null;
                        BIT_ANIM_Q_POOL.free(animQ);
                    }
                }
            }

            moveDelay = moveTotalDelay / (len * hei / 2);
            moveTime = 0;
            inAnimation = true;
        }

        public void draw(Batch batch)
        {
            int len = moveLen;
            int hei = moveHei;

            for(int ix = 0;ix < len;ix++)
            {
                for(int iy=0;iy<hei;iy++)
                {
                    BitAnimQ animQ = area[ix][iy];
                    if(animQ != null)
                    {
                        animQ.quadSprite.draw(batch);
                    }
                }
            }
        }

        abstract void prepareAnim(BitAnimQ animQ, int x, int y, int ix, int iy, float apply);

        public boolean act(float delta)
        {
            if(!inAnimation) return false;

            moveTime += delta;

            for(int x=0;x<moveLen;x++)
            {
                for(int y=0;y<moveHei;y++)
                {
                    float delay = (x * moveHei / 2  + y) * moveDelay;
                    if(moveTime > delay)
                    {
                        BitAnimQ animQ = area[x][y];
                        if(animQ != null)
                        {
                            float percent = 1.0f - (moveTime - delay) / moveTotalTime;
                            actAnim(animQ, percent < 0 ? 0 : percent);
                        }
                    }
                }
            }

            if(moveTime > moveTotalTime +  moveTotalDelay)
            {
                inAnimation = false;
                actFinished();
            }

            return inAnimation;
        }

        abstract void actAnim(BitAnimQ animQ, float percent);
        void actFinished() {}
    }

    class QuadAppearAnim extends QuadAreaAnim
    {
        public QuadAppearAnim()
        {
            moveInter = Interpolation.pow4In;
            moveTotalTime = 0.4f;
            moveTotalDelay = 0.6f;
        }

        @Override
        void prepareAnim(BitAnimQ animQ, int x, int y, int ix, int iy, float apply)
        {
            Sprite oldSprite = quads[x+ix][y+iy];
            animQ.set(oldSprite).setPosition(moveDeltaX * apply, moveDeltaY * apply);
            quads[x+ix][y+iy] = null;
        }

        @Override
        void actAnim(BitAnimQ animQ, float percent)
        {
            percent = moveInter.apply(percent < 0 ? 0 : percent);
            animQ.setPosition(moveDeltaX * percent, moveDeltaY * percent);
        }

        @Override
        void actFinished()
        {
            super.actFinished();
            for(int x = 0;x< area.length;x++)
            {
                for(int y = 0; y< area[x].length;y++)
                {
                    BitAnimQ animQ = area[x][y];
                    if(animQ != null)
                    {
                        animQ.restoreOrig();
                        quads[animQ.origCx][animQ.origCy] = animQ.quadSprite;
                    }
                }
            }
        }
    }

    class QuadDisappearAnim extends QuadAreaAnim implements Runnable
    {
        QuadArea area;
        float areaCenterFx, areaCenterFy;
        public QuadDisappearAnim(QuadArea area)
        {
            this.area = area;
            moveInter = Interpolation.pow4In;
            moveTotalTime = 0.4f;
            moveTotalDelay = 0.3f;

            Point point1 = new Point();
            Point point2 = new Point();

            field.getQuadCenterByXY(area.fromX, area.fromY, point1);
            field.getQuadCenterByXY(area.fromX + area.len -1, area.fromY +  area.hei -1, point2);

            areaCenterFx = (point1.x + point2.x)/2;
            areaCenterFy = (point1.y + point2.y)/2;
        }

        @Override
        void prepareAnim(BitAnimQ animQ, int x, int y, int ix, int iy, float apply)
        {
            int xi = x + ix;
            int yi = y + iy;
            Sprite oldSprite = quads[xi][yi];
            quads[xi][yi] = null;
            animQ.quadType = field.quads[xi][yi].used ? field.quads[xi][yi].type : Quad.TYPE_NONE;
            animQ.set(oldSprite);
        }

        @Override
        void actAnim(BitAnimQ animQ, float percent)
        {
            float ipercent = 1.0f - percent;
            if(animQ.quadType == Quad.TYPE_X2
                    || animQ.quadType == Quad.TYPE_M200
                    || animQ.quadType == Quad.TYPE_COINS
                    || animQ.quadType == Quad.TYPE_TIME)
            {
                animQ.quadSprite.setScale(1 + 10*ipercent);
            } else
            {
                float percentM = moveInter.apply(ipercent < 0 ? 0 : ipercent);
                float dx = animQ.origCenterX - areaCenterFx;
                float dy = animQ.origCenterY - areaCenterFy;
                if(dy == 0.0f)
                {
                    dx = Math.signum(dx);
                    if(dx == 0.0f) dy = -0.1f;
                } else
                {
                    float f = Math.abs(dx / dy);
                    if (f > 1)
                    {
                        dx = Math.signum(dx);
                        dy = 1 / f * Math.signum(dy);
                    } else
                    {
                        dx = f * Math.signum(dx);
                        dy = Math.signum(dy);
                    }
                }
                animQ.setPosition(moveDeltaX * percentM * dx, moveDeltaY * percentM * dy);
            }
            animQ.quadSprite.setAlpha(percent);
        }

        @Override
        void actFinished()
        {
            super.actFinished();
            NContext.current.post(this);
        }

        /**
         * called via post after animation is finished
         * process the result further
         */
        @Override
        public void run()
        {
            clearArea(true);
            NCore.busPost(new EQuadAreaHitMiddle(area));
        }
    }

    class LineStretch
    {
        Point  startPoint = new Point();
        Point  point1 = new Point();
        Point  point2 = new Point();
        Sprite ball1, ball2;
        Sprite line;

        String type;

        int factorX;
        int factorY;

        float percent = 0;
        float time = 0;
        float maxTime = 0.7f;

        boolean enabled = false;

        public LineStretch(String type)
        {
            this.type = type;
        }

        public void initGraphics(XMLLayoutLoader loader, TextureAtlas atlas)
        {
            line = loader.newSprite(type, atlas);
            ball1 = loader.newSprite("ball", atlas);
            ball2 = loader.newSprite("ball", atlas);
            enabled = false;
        }

        public void act(float delta)
        {
            if(!enabled) return;

            time += delta;
            if(time > maxTime) time = maxTime;

            percent = time / maxTime;

            float fx1 = startPoint.x - (startPoint.x - point1.x) * percent;
            float fy1 = startPoint.y - (startPoint.y - point1.y) * percent;

            float fx2 = startPoint.x + (point2.x - startPoint.x) * percent;
            float fy2 = startPoint.y + (point2.y - startPoint.y) * percent;

            ball1.setCenter(fx1, fy1);
            ball2.setCenter(fx2, fy2);
            if(factorX > 0)
            {
                line.setBounds(fx1, fy1 - line.getHeight()/2, fx2 - fx1, line.getHeight());
            } else
            {
                line.setBounds(fx1 - line.getWidth()/2, fy1, line.getWidth(), fy2 - fy1);
            }
        }

        public void draw(Batch batch)
        {
            if(!enabled || !game.isPlaying) return;

            line.draw(batch);
            if(time < maxTime)
            {
                ball1.draw(batch);
                ball2.draw(batch);
            }
        }

        public void init(int x, int y, int factorX, int factorY)
        {
            this.factorX = factorX;
            this.factorY = factorY;

            field.getQuadCenterByXY(x, y, startPoint);

            if(factorX > 0)
            {
                startPoint.y += config.quadOuterHalfHeight;
                point1.x = config.fieldDeltaXPix;
                point1.y = startPoint.y;
                point2.x = config.fieldDeltaXPix + config.fieldWidthPix;
                point2.y = startPoint.y;
            } else
            {
                startPoint.x -= config.quadOuterHalfWidth;
                point1.x = startPoint.x;
                point1.y = NContext.current.screenHeight - config.fieldDeltaYPix - config.fieldHeightPix;
                point2.y = NContext.current.screenHeight - config.fieldDeltaYPix;
                point2.x = startPoint.x;
            }

            percent = 0;
            time = 0;
            enabled = true;
        }
    }

    LineStretch lineStretch1 = new LineStretch("hlines");
    LineStretch lineStretch2 = new LineStretch("hlines");
    LineStretch lineStretch3 = new LineStretch("vlines");
    LineStretch lineStretch4 = new LineStretch("vlines");

    private void clearSelection()
    {
        for(int i=0;i<selected.size;i++)
        {
            BitAnimQ animQ = selected.get(i);
            animQ.restoreOrig();
            quads[animQ.origCx][animQ.origCy] = animQ.quadSprite;
            hitAnim.restoreOne(animQ.origCx, animQ.origCy, animQ.quadSprite);

            NCore.SPRITE_POOL.free(animQ.shadowSprite);
            BIT_ANIM_Q_POOL.free(animQ);
        }

        selected.clear();

        lineStretch1.enabled = false;
        lineStretch2.enabled = false;
        lineStretch3.enabled = false;
        lineStretch4.enabled = false;
    }


    @Subscribe
    public boolean onSelectQuad(EQuadSelect event)
    {
        Quad quad = event.quad;
        int selectionType = event.selectionType;

        if(selectionType != QuadCollector.QUAD_ADDED_SEQ)
        {
            clearSelection();
        }

        if(selectionType == QuadCollector.QUAD_HIT
            || selectionType == QuadCollector.QUAD_CLEAR_SEQ)
        {
            return true;
        }

        Sprite sprite = quads[quad.x][quad.y];

        if(sprite == null) return false;

        hitAnim.resetOne(quad.x, quad.y);

        BitAnimQ animQ = BIT_ANIM_Q_POOL.obtain();

        animQ.set(sprite);

        sprite.translate(-config.selectedDeltaPix, config.selectedDeltaPix);

        Sprite shadowSprite = NCore.SPRITE_POOL.obtain();
        shadowSprite.set(shadow);
        shadowSprite.setPosition(sprite.getX(), sprite.getY() + sprite.getHeight() - shadowSprite.getHeight());
        shadowSprite.setRotation(sprite.getRotation());

        animQ.origCx = quad.x;
        animQ.origCy = quad.y;
        animQ.shadowSprite = shadowSprite;

        selected.add(animQ);
        quads[quad.x][quad.y] = null;

        lineStretch1.init(quad.x, quad.y, 1, 0);
        lineStretch2.init(quad.x, quad.y+1, 1, 0);
        lineStretch3.init(quad.x, quad.y, 0, 1);
        lineStretch4.init(quad.x+1, quad.y, 0, 1);
        return true;
    }

    private void clearAnims()
    {
        for(IQuadAreaAnim anim : areaAnims)
        {
            anim.clearArea(false);
        }
        areaAnims.clear();
        hitAnim.clear();
    }

    class HitAnim
    {
        int numFlyers = 8;
        Sprite[] flyers = new Sprite[numFlyers];
        float flyersPercent[] = new float[numFlyers];
        float flFromX, flFromY, flToX, flToY;

        Sprite[] corners = new Sprite[4];

        boolean started = false;
        float time = 0;
        float maxTime = 0.6f;
        float maxDelay = 2.0f;

        QuadArea quadArea = new QuadArea(-1, -1, 0, 0);

        void load(XMLLayoutLoader loader, GraphicsInitContext gContext) {
            for(int i = 0;i < numFlyers;i++) {
                flyers[i] = loader.newSprite("glowball", gContext.atlas);
                flyers[i].setOriginCenter();
                flyers[i].setScale(1.0f - ((float)i / (float)numFlyers)*0.75f);
                flyersPercent[i] = 0;
            }
        }

        void init(QuadArea area)
        {
            reset();
            quadArea = area;

            corners[0]=quads[area.fromX][area.fromY];
            corners[1]=quads[area.fromX+area.len][area.fromY];
            corners[2]=quads[area.fromX+area.len][area.fromY+area.hei];
            corners[3]=quads[area.fromX][area.fromY+area.hei];

            int shift = Helpers.RND.nextInt(2);
            try {
                flFromX = corners[2 + shift].getX();
                flFromY = corners[2 + shift].getY();
                flToX = corners[0 + shift].getX();
                flToY = corners[0 + shift].getY();
            } catch (Exception e) {
                Log.w("game", "QuadArea NPE due to quads being in processing for the previous hit.");
                return;
            }
            Log.i("game", "Corners: " + flFromX + "," + flFromY + " -> " + flToX + "," + flToY);
            for(int i=0;i<numFlyers;i++) {
                flyers[i].setX(flFromX);
                flyers[i].setY(flFromY);
            }

            started = true;
            forward = true;
            prevForward = false;
            time = 0;
            maxDelay = 0.8f;
        }

        void resetOne(int x, int y)
        {
            if(x == quadArea.fromX)
            {
                if(y == quadArea.fromY)
                {
                    clearByIndex(0);
                }
                if(y == quadArea.fromY + quadArea.hei)
                {
                    clearByIndex(3);
                }
            }
            if(x == quadArea.fromX + quadArea.len)
            {
                if(y == quadArea.fromY)
                {
                    clearByIndex(1);
                }
                if(y == quadArea.fromY + quadArea.hei)
                {
                    clearByIndex(2);
                }
            }
        }

        void restoreOne(int x, int y, Sprite sprite)
        {
            if(x == quadArea.fromX)
            {
                if(y == quadArea.fromY)
                {
                    restoreByIndex(0, sprite);
                }
                if(y == quadArea.fromY + quadArea.hei)
                {
                    restoreByIndex(3, sprite);
                }
            }
            if(x == quadArea.fromX + quadArea.len)
            {
                if(y == quadArea.fromY)
                {
                    restoreByIndex(1, sprite);
                }
                if(y == quadArea.fromY + quadArea.hei)
                {
                    restoreByIndex(2, sprite);
                }
            }
        }

        private void restoreByIndex(int idx, Sprite sprite)
        {
            for(Sprite copyFrom : corners)
            {
                if(copyFrom != null)
                {
                    corners[idx] = sprite;
                    sprite.setOriginCenter();
                    sprite.setScale(copyFrom.getScaleX());
                    sprite.setRotation(copyFrom.getRotation());
                }
            }
        }

        private void clearByIndex(int idx) {
            if(corners[idx] != null)
            {
                corners[idx].setScale(1);
                corners[idx].setRotation(0);
                corners[idx] = null;
            }
        }

        void reset()
        {
            for(int i=0;i<corners.length;i++)
            {
                if(corners[i]!= null)
                {
                    corners[i].setScale(1);
                    corners[i].setRotation(0);
                    corners[i] = null;
                }
            }
            started = false;
            quadArea = new QuadArea(-1, -1, 0, 0);
            Arrays.fill(flyersPercent, 0);
        }

        void clear()
        {
            started = false;
            Arrays.fill(corners, null);
            Arrays.fill(flyersPercent, 0);
        }

        boolean forward = true;
        boolean prevForward = false;

        void act(float delta)
        {
            if(!started) return;

            time += delta;

            if(time < maxDelay) {
                doAction(maxTime, prevForward);
                return;
            }

            float actTime = time - maxDelay;

            if(actTime >= maxTime)
            {
//                started = false;
                doAction(maxTime, forward);
                time = 0;
                maxDelay = 0.6f;
                prevForward = forward;
                forward = !forward;
                if(!prevForward) {
                    Arrays.fill(flyersPercent, flyersPercent[0]);
                    doAction(maxTime, false);
                }
            } else
            {
                doAction(actTime, forward);
            }
        }

        void doAction(float actTime, boolean forward) {
            float percent = Interpolation.pow2In.apply(actTime / maxTime);
            percent = forward ? percent : 1 - percent;
            float scale = 1 - percent * 0.4f;
            float angle = 90 * percent;
            for (Sprite corner : corners) {
                if (corner != null) {
                    corner.setScale(scale);
                    corner.setRotation(angle);
                }
            }

            flyersPushPercent(percent);

            for (int i = 0; i < numFlyers; i++) {
                float x = flFromX + (flToX - flFromX) * flyersPercent[i];
                float y = flFromY + (flToY - flFromY) * flyersPercent[i];
                flyers[i].setX(x);
                flyers[i].setY(y);
            }
        }

        void flyersPushPercent(float percent) {
            for(int i=numFlyers - 1;i > 0; i--) {
                flyersPercent[i] = flyersPercent[i-1];
            }
            flyersPercent[0] = percent;
        }

        void draw(Batch batch)
        {
            if(!started) return;

            if(forward || time < maxDelay) {
                for (int i = 0; i < numFlyers; i++) {
                    flyers[i].draw(batch);
                }
            }
        }
    }

    HitAnim hitAnim = new HitAnim();

    class ColorChanger implements IQuadAreaAnim
    {
        int fromType;
        int toType;
        int row = 0;
        float time = 0;
        float maxTime = 0.05f;
        float initDelay = 1.1f;

        public ColorChanger(Quad.MatchResult result)
        {
            if(result.quad.type == result.type)
            {
                fromType = Math.abs(result.quad.type);
                toType = Math.abs(result.quad.subtype);
            } else
            {
                fromType = Math.abs(result.quad.subtype);
                toType = Math.abs(result.quad.type);
            }
        }

        @Override
        public boolean act(float delta)
        {
            time += delta;

            if(initDelay > 0)
            {
                if(time > initDelay)
                {
                    time = 0;
                    initDelay = 0;
                    NCore.instance().getSoundman().playSound(ISoundMan.SND_RECOLOR, false);
                }
                return true;
            }

            if(time >= maxTime)
            {
                time = 0;
                Field fld = field;
                boolean gotIt = false;
                do
                {
                    for (int x = 0; x < fld.width; x++)
                    {
                        Quad quad = fld.quads[x][row];
                        if (quad.type == fromType)
                        {
                            Sprite old = quads[x][row];
                            if(old != null)
                            {
                                quad.type = toType;
                                Sprite newS = newFieldSprite(x, row);
                                newS.setPosition(old.getX(), old.getY());
                                newS.setRotation(old.getRotation());
                                quads[x][row] = newS;
                                NCore.SPRITE_POOL.free(old);
                                gotIt = true;
                            }
                        }
                    }
                    row++;
                } while (!gotIt && row < fld.height);
            }
            if(row >= field.height) NCore.busPost(new EFieldChanged());
            return row < field.height;
        }

        @Override
        public void clearArea(boolean dispose)
        {

        }

        @Override
        public void prepare(int x, int y, int len, int hei)
        {

        }

        @Override
        public void draw(Batch batch)
        {
        }
    }

    @Subscribe
    public void onQuadAreaHitStart(EQuadAreaHitStart event)
    {
        QuadArea area = event.area;
        QuadDisappearAnim anim = new QuadDisappearAnim(area);
        anim.prepare(area.fromX, area.fromY, area.len, area.hei);
        areaAnims.add(anim);
        hitAnim.reset();
    }

    @Subscribe
    public void onQuadAreaHitFinish(EQuadAreaHitFinish event)
    {
        QuadArea area = event.area;

        initFieldArea(area.fromX, area.fromY, area.len, area.hei);

        QuadAppearAnim anim = new QuadAppearAnim();
        anim.prepare(area.fromX, area.fromY, area.len, area.hei);
        NCore.instance().getSoundman().playSound(ISoundMan.SND_QUAD_AREA_APPEAR, false);
        areaAnims.add(anim);
        if(event.collector.needRecolor())
        {
            ColorChanger changer = new ColorChanger(event.collector.matchedBy);
            areaAnims.add(changer);
        }
    }

    @Subscribe
    public void onGameLevelUp(EGameLevelUp event)
    {
        clearSelection();
        clearAnims();

        QuadArea area = new QuadArea(0, 0, field.width, field.height);
        QuadDisappearAnim anim = new QuadDisappearAnim(area);
        anim.prepare(area.fromX, area.fromY, area.len, area.hei);
        areaAnims.add(anim);
    }

    @Subscribe
    public void onFullReshuffle(EFullReshuffle event)
    {
        clearSelection();
        clearAnims();

        QuadArea area = new QuadArea(0, 0, field.width, field.height);
        QuadDisappearAnim anim = new QuadDisappearAnim(area);
        anim.prepare(area.fromX, area.fromY, area.len, area.hei);
        areaAnims.add(anim);
    }

    @Subscribe
    public void onSolutionFound(ESolutionFound event)
    {
    }

    @Subscribe
    public void onPerkShowHint(EPerkShowHint event)
    {
        hitAnim.init(event.solution);
    }
}
