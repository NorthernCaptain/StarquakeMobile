package northern.captain.quadronia.game.profile;

import java.util.ArrayList;
import java.util.List;

import northern.captain.quadronia.game.IGameContext;

/**
 * Created by leo on 26.05.15.
 */
public class SKUManager
{
    public static SKUManager instance = new SKUManager();

    private List<SKUItem> items = new ArrayList<SKUItem>();

    public SKUManager()
    {
        items.add(new SKUItem(IGameContext.BONUS_JOKER_IDX));
        items.add(new SKUItem(IGameContext.BONUS_BOMB_IDX));
        items.add(new SKUItem(IGameContext.BONUS_SWAP_IDX));
    }

    public List<SKUItem> getItems()
    {
        return items;
    }
}
