package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.profile.UserBase;

/**
 * Created by leo on 18.09.15.
 */
public class EUserUpdated implements BusEvent
{
    public UserBase user;

    public EUserUpdated(UserBase user)
    {
        this.user = user;
    }
}
