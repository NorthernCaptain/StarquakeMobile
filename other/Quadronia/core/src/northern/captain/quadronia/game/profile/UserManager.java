package northern.captain.quadronia.game.profile;

import java.util.ArrayList;
import java.util.List;

import northern.captain.gamecore.glx.NCore;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.EUserUpdated;

/**
 * Created by leo on 21.05.15.
 */
public class UserManager
{
    public static UserManager instance = new UserManager();

    protected List<UserBase> users = new ArrayList<UserBase>();
    protected IGameContext context;

    protected UserBase currentUser;
    private boolean inited = false;

    public UserManager()
    {

    }

    public UserBase newUser()
    {
        return newUser(-1);
    }

    public UserBase newUser(int idx)
    {
        int newIdx = idx;
        if(idx < 0) newIdx = NativeNFactory.nci.w(idx);
        return new UserBase(newIdx);
    }

    public void init(IGameContext context)
    {
        if(inited) return;

        this.context = context;
        int totalUsers = context.getTotalPlayers();
        for(int i=0;i<totalUsers;i++)
        {
            UserBase user = newUser(i);
            users.add(user);
        }

        if(totalUsers == 0)
        {
            UserBase user = newUser();
            users.add(user);
        }

        int curUserIdx = context.getCurrentPlayerIdx();
        currentUser = users.get(curUserIdx);
        context.setCurrentPlayer(currentUser);
        inited = true;
    }

    public UserBase getCurrentUser()
    {
        return currentUser;
    }

    public void setUserInfo(String name, String googleId)
    {
        if(!inited)
        {
            init(Game.defaultGameContext);
        }

        if(UserBase.UNKNOWN_NAME.equals(currentUser.getName()))
        {
            currentUser.setName(name);
        }

        currentUser.setGoogleId(googleId);
        NCore.busPost(new EUserUpdated(currentUser));
    }
}
