package northern.captain.tools;

/**
 * Created by leo on 29.03.15.
 */
public class Randomizer
{
    private int delta;
    private int[] hits;

    public Randomizer(int max, int delta)
    {
        hits = new int[max];
        this.delta = delta;
    }

    public int next()
    {
        int ret = 0;
        int max = 0;
        for(int i=0;i<hits.length;i++)
        {
            if(hits[i] > max) max=hits[i];
        }

        int min = max - delta;
        int low = 0;
        for(int i=0;i<hits.length;i++)
        {
            if(hits[i]<min) low++;
        }

        if(low == 0)
        {
            ret = Helpers.RND.nextInt(hits.length);
            hits[ret]++;
            return ret;
        }

        if(low == 1)
        {
            ret = 0;
        } else
        {
            ret = Helpers.RND.nextInt(low);
        }

        low = 0;
        for(int i=0;i<hits.length;i++)
        {
            if(hits[i] < min)
            {
                if(low == ret)
                {
                    hits[i]++;
                    return i;
                }
                low++;
            }
        }

        ret = Helpers.RND.nextInt(hits.length);
        hits[ret]++;
        return ret;
    }
}
