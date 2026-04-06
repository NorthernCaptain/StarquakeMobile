package northern.captain.quadronia.game.core;

/**
 * Created by leo on 08.11.15.
 */
public class QuadCheck
{
    protected Field field;
    /**
     * Mathed by special type. Here we have super-type that matched
     */
    public Quad.MatchResult matchedBy = new Quad.MatchResult();
    protected Quad.MatchResult curMatch = new Quad.MatchResult();

    protected Quad[] checkQ = new Quad[4];
    public boolean checkHit(Quad quad1, Quad quad2)
    {
        checkQ[0] = quad1;
        checkQ[1] = quad2;
        checkQ[2] = field.quads[quad1.x][quad2.y];
        checkQ[3] = field.quads[quad2.x][quad1.y];

        if(checkQ[2].type == Quad.TYPE_MULTI ||
            checkQ[3].type == Quad.TYPE_MULTI ||
            quad1.type == Quad.TYPE_MULTI ||
            quad2.type == Quad.TYPE_MULTI)
            return true;

        for(int i=0;i<3;i++)
        {
            for(int j = i+1;j<4;j++)
            {
                if(!checkQ[i].match2(checkQ[j], curMatch)) return false;
                if(matchedBy.type == Quad.TYPE_NONE || curMatch.quad.type < 0)
                {
                    matchedBy.set(curMatch);
                }
            }
        }

        return true;
    }
}
