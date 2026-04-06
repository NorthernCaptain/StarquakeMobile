package northern.captain.quadronia.game.events;

import northern.captain.quadronia.IGameOptionsMenu;

/**
 * Created by leo on 20.10.15.
 */
public class EOptionsChanged
{
    public IGameOptionsMenu optionsMenu;
    public String optionName;

    public EOptionsChanged(IGameOptionsMenu optionsMenu, String optionName)
    {
        this.optionsMenu = optionsMenu;
        this.optionName = optionName;
    }
}
