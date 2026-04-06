package northern.captain.quadronia.android;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.quadronia.IGameOptionsMenu;
import northern.captain.quadronia.android.common.AndroidContext;
import northern.captain.quadronia.game.events.EOptionsChanged;
import northern.captain.tools.IPersistCfg;
import northern.captain.tools.IPersistentConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class GameOptionsMenu implements IPersistCfg, IGameOptionsMenu
{
	private boolean soundOn = true;
	private boolean vibrationOn = true;
	private boolean statusBarOn = false;
	private boolean musicOn = true;
	private boolean tutorialOn = true;

	private IPersistentConfig cfg;
	
	public GameOptionsMenu()
	{
//		NCore.instance().addPersistListener(this);
	}
	
	/**
	 * Create menu from xml resources
	 * @param menu
	 */
	public void inflateMenu(Menu menu)
	{
		setLang();
	}
	
	/**
	 * Prepare menu according to the state of options
	 * @param menu
	 */
	public void prepareMenu(Menu menu)
	{
		setMenuState(menu);
	}
	
	private void setMenuState(Menu menu)
	{

	}
	
	public boolean menuItemSelected(MenuItem item)
	{
		return true;
	}

    @Override
	public void openFeedback()
	{
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"navalclash@gmail.com"});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback from user: "
				+ UserManager.instance.getCurrentUser().getName());
		emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Quadronia team,\n");
		AndroidContext.activity.startActivity(Intent.createChooser(emailIntent, "Email to Developer"));
		
	}
	
	private String[] langs = new String[] 
	                                    {
											"English",
											"Spanish (Español)",
											"French (Française)",
											"Italian (Italiano)",
											"Russian (Русский)",
											"Hungarian (Magyar)",
											"Polish (Polski)"
	                                    };
	private String[] langAbb = new String[]
	                                      {
			"en",
			"es",
			"fr",
			"it",
			"ru",
			"hu",
			"pl"
	                                      };
	private int langIdx = -1;
	
	@Override
    public void setLang(int idx)
	{
		if(idx < 0)
			return;
//		Resources res = SeabattleNCAE.activity.getResources();
//		// Change locale settings in the app.
//		DisplayMetrics dm = res.getDisplayMetrics();
//		android.content.res.Configuration conf = res.getConfiguration();
//		conf.locale = new Locale(langAbb[idx]);
//		res.updateConfiguration(conf, dm);
//		langIdx = idx;
		
		SharedRes.instance.reloadLang(langAbb[idx]);
        langIdx = idx;
	}
	
	@Override
    public void setLang()
	{
		setLang(langIdx);
	}
	
	@Override
    public String getAppLang()
	{
		if(langIdx < 0)
		{
			String lang = Locale.getDefault().getLanguage();
            for(String langA : langAbb)
            {
                if(langA.equals(lang))
                    return lang;
            }
			return "en";
		}
		return langAbb[langIdx];
	}
	
	@Override
    public void showLangChooser()
	{
//		ArrayAdapter<String> adap = new ArrayAdapter<String>(AndroidContext.activity,
//				R.layout.simple_list_item,
//				langs);
//        final AlertDialog dlg = new AlertDialog.Builder(AndroidContext.activity)
//        .setTitle(R.string.langChoose)
//        .setSingleChoiceItems(adap, langIdx, new DialogInterface.OnClickListener()
//        	{
//                public void onClick(DialogInterface dlg, int which)
//                {
//                	dlg.dismiss();
//                	setLang(which);
//                	Gdx.app.postRunnable(new Runnable()
//					{
//
//						@Override
//						public void run()
//						{
//                            MyToast.toast(MyToast.ALWAYS, "statusBarWarn", true);
//						}
//					});
//                }
//        })
//        .create();
//		dlg.show();
//
	}

    @Override
    public void flipSoundOption()
	{
		soundOn = soundOn ? false : true;
		cfg.setBoolean("isSound", soundOn);
		NCore.busPost(new EOptionsChanged(this, "sound"));
	}
	
	@Override
    public void flipVibroOption()
	{
		vibrationOn = vibrationOn ? false : true;
		cfg.setBoolean("isVibration", vibrationOn);
		NCore.busPost(new EOptionsChanged(this, "vibration"));
		if(vibrationOn) NCore.instance().getSoundman().vibrate();
	}
	
	@Override
    public void flipStatusBarOption()
	{
        if(NContext.current.isAllowStatusBar())
        {
    		statusBarOn = statusBarOn ? false : true;
    		cfg.setBoolean("statusBar", statusBarOn);
//    		MyToast.toast(MyToast.ALWAYS, "statusBarWarn", true);
			NCore.busPost(new EOptionsChanged(this, "statusBar"));
        }
	}

	@Override
	public void flipMusinOption()
	{
		musicOn = musicOn ? false : true;
		cfg.setBoolean("isMusic", musicOn);
		NCore.busPost(new EOptionsChanged(this, "music"));
	}

	@Override
	public void flipTutorial()
	{
		tutorialOn = tutorialOn ? false : true;
		cfg.setBoolean("isTutorial", tutorialOn);
		NCore.busPost(new EOptionsChanged(this, "tutorial"));
	}

	public void loadData(IPersistentConfig cfg)
	{
		soundOn = cfg.getBoolean("isSound", true);
		vibrationOn = cfg.getBoolean("isVibration", true);
		statusBarOn = cfg.getBoolean("statusBar", false);
		musicOn = cfg.getBoolean("isMusic", false);
		tutorialOn = cfg.getBoolean("isTutorial", false);
		this.cfg = cfg;
		langIdx = cfg.getInt("langidx", -1);
		setLang(langIdx);
		themeIndex = cfg.getInt("themeidx", themeIndex);
		NCore.busPost(new EOptionsChanged(this, "loaded"));
//        NContext.current.setLang(getAppLang());
	}

	@Override
    public boolean isSoundOn()
	{
		return this.soundOn;
	}
	
	@Override
    public boolean isVibrationOn()
	{
		return this.vibrationOn;
	}
	
	@Override
    public boolean isStatusBarOn()
	{
		return statusBarOn && NContext.current.isAllowStatusBar();
	}

	@Override
	public boolean isMusicOn()
	{
		return musicOn;
	}

	@Override
	public boolean isTutorialOn()
	{
		return tutorialOn;
	}

	public void loadData(FileInputStream fin)
	{
	}

	public void saveData(IPersistentConfig cfg)
	{
		cfg.setBoolean("isSound", soundOn);
		cfg.setBoolean("isVibration", vibrationOn);
		cfg.setInt("langidx", langIdx);
		cfg.setBoolean("statusBar", statusBarOn);
		cfg.setBoolean("isMusic", musicOn);
		cfg.setBoolean("isTutorial", tutorialOn);
		cfg.setInt("themeidx", themeIndex);
	}

	public void saveData(FileOutputStream fout)
	{
	}

	private int themeIndex = 1;

	@Override
	public int getThemeIndex()
	{
		return themeIndex;
	}

	@Override
	public void setThemeIndex(int newIdx)
	{
		themeIndex = newIdx;
		cfg.setInt("themeidx", themeIndex);
		NCore.busPost(new EOptionsChanged(this, "theme"));
	}
}
