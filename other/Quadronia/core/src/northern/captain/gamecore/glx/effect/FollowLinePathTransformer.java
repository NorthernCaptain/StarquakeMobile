package northern.captain.gamecore.glx.effect;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.List;

import northern.captain.quadronia.game.Point;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 07.09.13
 * Time: 22:44
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class FollowLinePathTransformer implements IParticleTransformer
{
    float totalTime, currentTime;
    float totalLength = 0;
    float currentLength;
    int currentIndex;

    public int currentX, currentY;
    public float percent;

    boolean started = false;

    Point currentPoint = new Point(0, 0);

    static public class Point
    {
        public int x, y;
        public float lengthPoint;

        public Point() {}

        public Point(int ix, int iy)
        {
            x = ix;
            y = iy;
        }

        public void init(int ix, int iy)
        {
            x = ix;
            y = iy;
            lengthPoint = 0;
        }
    }

    List<Point> points = new ArrayList<Point>();

    static Pool<Point> pointPool = new Pool<Point>()
    {
        @Override
        protected Point newObject()
        {
            return new Point();
        }
    };

    public FollowLinePathTransformer(float totalTime)
    {
        this.totalTime = totalTime;
    }

    public FollowLinePathTransformer()
    {
        totalTime = 0.7f;
    }

    @Override
    public void clear()
    {
        currentTime = 0;
        totalLength = 0;
        currentLength = 0;
        currentIndex = 0;
        started = false;
        percent = 0;
        for(Point point : points)
        {
            pointPool.free(point);
        }
        points.clear();
    }


    public float getTotalTime()
    {
        return totalTime;
    }

    public void setTotalTime(float totalTime)
    {
        this.totalTime = totalTime;
    }

    public float addPoint(int x, int y)
    {
        float length = 0;
        Point point = pointPool.obtain();
        point.init(x, y);

        if(!points.isEmpty())
        {
            Point prev = points.get(points.size()-1);
            length = lineLength(prev, point);
            totalLength += length;
            point.lengthPoint = totalLength;
        } else
        {
            currentX = x;
            currentY = y;
        }
        points.add(point);

        return length;
    }

    private float lineLength(Point p1, Point p2)
    {
        return (float)Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }

    @Override
    public void start()
    {
        if(points.isEmpty()) return;

        Point point = points.get(0);
        currentTime = 0;
        currentLength = 0;
        currentIndex = 0;
        currentX = point.x;
        currentY = point.y;
        started = true;
    }

    /**
     * Update (extend) transformer for a given number of seconds
     *
     * @param delta
     */
    @Override
    public void update(float delta)
    {
        if(!started)
        {
            start();
        }

        if(!started) return;

//        if(currentTime >= totalTime)
//        {
//            started = false;
//        }

        currentTime += delta;
        if(currentTime >= totalTime)
        {
            currentTime = totalTime;
        }

        percent = currentTime / totalTime;
        currentLength = totalLength * percent;

        currentIndex = pointByLength(currentLength, currentPoint, 0);

        currentX = currentPoint.x;
        currentY = currentPoint.y;
    }

    public float updateTime(float delta)
    {
        currentTime += delta;
        if(currentTime >= totalTime)
        {
            currentTime = totalTime;
        }

        percent = currentTime / totalTime;
        return percent;
    }

    public int pointByLength(float lineLength, Point result, int startIndex)
    {
        Point p1 = null, p2 = null;

        int i;
        int siz = points.size();

        if(siz == 0)
        {
            throw new IndexOutOfBoundsException("points size = 0");
        }

        for(i=(startIndex < 0 ? 0 : startIndex);i<siz;i++)
        {
            Point point = points.get(i);

            if(point.lengthPoint <= lineLength)
            {
                p1 = point;
                continue;
            }

            if(point.lengthPoint >= lineLength)
            {
                p2 = point;
                break;
            }
        }

        if(p2 == null)
        {
            result.x = p1.x;
            result.y = p1.y;
            return i;
        }

        float len = lineLength(p1, p2);
        float deltaLen = lineLength - p1.lengthPoint;
        float percent = deltaLen / len;

        result.x = (int)(p1.x + (p2.x - p1.x)*percent);
        result.y = (int)(p1.y + (p2.y - p1.y)*percent);

        return i;
    }
    /**
     * Transform the effect according to the current transformer state
     *
     * @param effect
     * @param actor  - could be null
     */
    @Override
    public void transform(ParticleEffect effect, ParticleEffectActor actor)
    {
        effect.setPosition(currentX, currentY);
//        if(!started)
//        {
//            actor.hide();
//        }
    }

    /**
     * Gets total duration of this transformation in milliseconds
     *
     * @return
     */
    @Override
    public int getDurationMillis()
    {
        return (int)(totalTime * 1000);
    }

    public float getTotalLength()
    {
        return totalLength;
    }
}
