package northern.captain.tools;

import northern.captain.gamecore.glx.NCore;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MarketOpener implements IPersistCfg
{
	protected String marketLink = "https://play.google.com/store/apps/dev?id=7071592005002539975";
	protected static MarketOpener singleton = new MarketOpener();
	
	public static MarketOpener instance()
	{
		return singleton;
	}

    public static void setSingleton(MarketOpener opener)
    {
        singleton = opener;
    }
	
	public MarketOpener()
	{
	}
	
	public MarketOpener(String uriString)
	{
		marketLink = uriString;
	}
	
	public void showMarket(NCore app)
	{
	}
	
	public void setMarketLink(String newLink)
	{
		marketLink = newLink;
	}

    public String getMarketLink()
    {
        return marketLink;
    }

    @Override
	public void saveData(IPersistentConfig cfg)
	{
		cfg.setString("marketLnk", marketLink);
	}

	@Override
	public void saveData(FileOutputStream fout)
	{
	}

	@Override
	public void loadData(IPersistentConfig cfg)
	{
		//marketLink = cfg.getString("marketLnk", marketLink);
	}

	@Override
	public void loadData(FileInputStream fin)
	{
	}
}
