package northern.captain.quadronia.game.profile;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.core.Game;
import northern.captain.tools.StringUtils;

/**
 * Created by leo on 21.05.15.
 */
public class UserBase
{
    public static final int USER_BASE_IDX = 100000;
    public static final int USER_SPACE = 100000;
    public static final String UNKNOWN_NAME = "Somebody";

    private int id;
    private int idx;
    private String name;
    private String googleId;
    private boolean undefined = true;

    private IGameContext context;

    public UserBase(int index)
    {
        idx = index;
        id = index2Id(idx);

        context = Game.defaultGameContext;
        name = context.getPlayerName(id);
        googleId = context.getPlayerGID(id);
        if(googleId == null) googleId = "-";

        if(StringUtils.isNullOrEmpty(name))
        {
            name = UNKNOWN_NAME;
        } else
        {
            undefined = false;
        }
    }

    public String getName()
    {
        return name;
    }

    public boolean setName(String name)
    {
        if(StringUtils.isNullOrEmpty(name) || UNKNOWN_NAME.equals(name))
        {
            return false;
        }

        if(name.length()> 15)
        {
            name = name.substring(0, 14);
        }

        this.name = name;
        context.setPlayerName(id, name);
        undefined = false;
        return true;
    }

    public void setGoogleId(String googleId)
    {
        this.googleId = googleId;
        if(googleId == null)
        {
            this.googleId="-";
        }

    }

    public int getId()
    {
        return id;
    }

    public int getIdx()
    {
        return idx;
    }

    public boolean isUndefined()
    {
        return undefined;
    }

    public int getCoins()
    {
        return context.getUserDatai(id, IGameContext.PLAYER_COINS_IDX);
    }

    public void setCoins(int coins)
    {
        context.setDatai(id + IGameContext.PLAYER_COINS_IDX, coins);
    }

    public void addCoins(int coinsToAdd)
    {
        context.setDatai(id + IGameContext.PLAYER_COINS_IDX, coinsToAdd + getCoins());
    }

    public static int index2Id(int index)
    {
        return USER_BASE_IDX + index * USER_SPACE;
    }
}
