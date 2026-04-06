package northern.captain.quadronia.game.profile;

import northern.captain.quadronia.game.IGameContext;
import northern.captain.gamecore.glx.res.SharedRes;

/**
 * Created by leo on 26.05.15.
 */
public class SKUItem
{
    public int id;
    public int price;
    public int type;
    public String dispName;
    public String dispDesc;
    public int reservedQty;

    private IGameContext context;

    public SKUItem(int type)
    {
        this.type = type;
        id = type + IGameContext.PRICE_DELTA_IDX;
    }

    public void init(IGameContext context)
    {
        this.context = context;
        price = context.getUserDatai(0, id);
        dispName = SharedRes.instance.x("item" + type);
        dispDesc = SharedRes.instance.x("help" + type);

        reservedQty = 0;
    }

    public int getPrice()
    {
        return price;
    }

    public int getQty()
    {
        return context.getBonusByIdx(type);
    }

    public int getTotalQty()
    {
        return getQty() + reservedQty;
    }

    public boolean isLocked()
    {
        return false;
    }

    public String getIconName()
    {
        return "itemic" + id;
    }

    public void processReserve()
    {
        context.internalPurchase(type, reservedQty, reservedQty*price);
        reservedQty = 0;
    }

}
