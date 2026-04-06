package northern.captain.gamecore.glx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Pool;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import northern.captain.quadronia.IGameOptionsMenu;
import northern.captain.quadronia.SetupDialog;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenContext;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.tools.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 05.06.14
 * Time: 19:09
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public abstract class NCore implements INCore
{
    public static final Pool<Sprite> SPRITE_POOL = new Pool<Sprite>()
    {
        @Override
        protected Sprite newObject()
        {
            return new Sprite();
        }
    };

    protected static Bus BUS = new Bus(ThreadEnforcer.ANY);

    protected static NCore singleton;
    public static NCore instance()
    {
        return singleton;
    }

    protected UUID uuid = UUID.fromString(getUUIDString());

    @Override
    public String getUUIDString()
    {
        return "896babb9-7b41-49fa-82f2-da4900ae1f0e";
    }

    @Override
    public UUID getUUID()
    {
        return uuid;
    }

    //===============================================================
    /**
     * VERSION code! CURRENT! Do not forget to change before release
     */
    public int version = 1041;
    public String versionName = "1.4.0";

    //===============================================================


    public static void busRegister(Object objectToReg)
    {
        try
        {
            BUS.register(objectToReg);
        }
        catch (Exception ex)
        {
            Log.e("ncgame", "BUS register exception!", ex);
        }
    }

    public static void busUnregister(Object object)
    {
        try
        {
            BUS.unregister(object);
        }
        catch(Exception ex)
        {
            Log.e("ncgame", "BUS UNregister exception!", ex);
        }
    }

    public static void busPost(Object event)
    {
        BUS.post(event);
    }


    protected ISoundMan soundman;

    @Override
    public void setSoundman(ISoundMan sound)
    {
        soundman = sound;
    }

    @Override
    public ISoundMan getSoundman()
    {
        return soundman;
    }


    private List<IPersistCfg> needSave = new ArrayList<IPersistCfg>();
    private IPersistentConfig cfg;

    @Override
    public IPersistentConfig getCfg()
    {
        if(cfg == null)
            cfg = PersistentConfigFactory.instance.newConfig();
        return cfg;
    }

    /* (non-Javadoc)
     * @see northern.captain.gamecore.glx.INCore#addPersistListener(northern.captain.tools.IPersistCfg)
     */
    @Override
    public void addPersistListener(IPersistCfg persistObj)
    {
        needSave.add(persistObj);
    }

    /* (non-Javadoc)
     * @see northern.captain.gamecore.glx.INCore#removePersistListener(northern.captain.tools.IPersistCfg)
     */
    @Override
    public void removePersistListener(IPersistCfg persistObj)
    {
        needSave.remove(persistObj);
    }


    /* (non-Javadoc)
     * @see northern.captain.gamecore.glx.INCore#getOurPackageName()
     */
    @Override
    public String getOurPackageName()
    {
        return "northern.captain.quadronia.android";
    }

    /* (non-Javadoc)
     * @see northern.captain.gamecore.glx.INCore#getScreenFlow()
     */
    @Override
    public IScreenWorkflow getScreenFlow()
    {
        return screenflow;
    }

    protected IScreenWorkflow screenflow;



    /**
     * Save all data to persistent storage
     */
    public void saveData()
    {
        //Not initialized yet but already stopping - do not save anything
        if(cfg == null) return;

        cfg.setAutoCommit(false);
        StorageDir.setNeedStore(true);
        IPersistCfg ar[] = new IPersistCfg[needSave.size()];
        needSave.toArray(ar);
        for (IPersistCfg obj : ar)
            obj.saveData(cfg);

        MarketOpener.instance().saveData(cfg);
        cfg.setInt("vc", version);
        RateTracker.instance().saveData(cfg);
        GoogleGamesFactory.instance().getProcessor().saveData(cfg);
        gameOptionsMenu.saveData(cfg);
        cfg.commitChanges();
        cfg.setAutoCommit(true);
        StorageDir.setNeedStore(false);
    }

    /**
     * Loads data from the storage
     */
    public void loadData()
    {
        cfg = getCfg();
        IPersistCfg[] toload = needSave.toArray(new IPersistCfg[0]);
        for (IPersistCfg obj : toload)
            obj.loadData(cfg);
        UserManager.instance.init(Game.defaultGameContext);
        MarketOpener.instance().loadData(cfg);
        RateTracker.instance().loadData(cfg);
        GoogleGamesFactory.instance().getProcessor().loadData(cfg);
        gameOptionsMenu.loadData(cfg);
    }


    public void saveImmediateData()
    {
        IPersistentConfig cfg = PersistentConfigFactory.instance.newConfig();
        cfg.setAutoCommit(false);
        screenflow.saveData(cfg);
        cfg.commitChanges();
    }

    protected Stack<IOnBackAction> backActionStack = new Stack<IOnBackAction>();
    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed()
    {
        if(backActionStack.empty())
        {
            screenflow.prepare(IScreenWorkflow.WF_BACK);
            screenflow.doAction(IScreenWorkflow.WF_BACK);
        } else
        {
            IOnBackAction onBackAction = backActionStack.peek();
            onBackAction.OnBackPressed();
        }
    }

    @Override
    public void pushOnBackAction(IOnBackAction action)
    {
        if(action != null)
            backActionStack.push(action);
    }

    @Override
    public void popOnBackAction(IOnBackAction action)
    {
        if(backActionStack.empty())
            return;

        if(action == null)
        {
            backActionStack.pop();
        } else
        {
            backActionStack.remove(action);
        }
    }

    @Override
    public void activate(ScreenContext sctx)
    {
        doQuit();
    }

    @Override
    public void loadOnIntro()
    {
    }

    @Override
    public void onDestroy()
    {
        BUS = null;
    }

    protected List<IActivityListener> activityListeners = new ArrayList<IActivityListener>();

    @Override
    public void addActivityResultListener(IActivityListener listener)
    {
        if(listener == null)
            return;
        removeActivityResultListener(listener.getClass());
        activityListeners.add(listener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Object data)
    {
        for(IActivityListener listener : activityListeners)
        {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void removeActivityResultListener(Class<? extends IActivityListener> clazz)
    {
        for(IActivityListener listener : activityListeners)
        {
            if(listener.getClass().equals(clazz))
            {
                activityListeners.remove(listener);
                return;
            }
        }
    }


    @Override
    public void doQuit()
    {
        Gdx.app.exit();
    }

    @Override
    public boolean isNewVersion()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetNewVersion()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getStopped()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setStopped(boolean stopped)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLang()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void openSoftKeyboard(Object from)
    {
        Gdx.input.setOnscreenKeyboardVisible(true);
    }

    @Override
    public void closeSoftKeyboard(Object from)
    {
        Gdx.input.setOnscreenKeyboardVisible(false);
    }

    @Override
    public void deactivate(ScreenContext sctx)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onBackAction()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onFullSuspend()
    {
        if(screenflow != null)
            screenflow.onFullSuspend();

        NativeNFactory.nci.s(5843);
        NCore.instance().saveData();
    }

    @Override
    public void onFullResume()
    {
        if(screenflow != null)
            screenflow.onFullResume();
    }

    @Override
    public void prepareEnter()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void openOptionsMenu()
    {
        if(screenflow.getCurrentScreen() == null)
            return;

        NContext.current.resetLastNano();
        SetupDialog dlg = new SetupDialog();
        dlg.show();
    }

    protected IGameOptionsMenu gameOptionsMenu;

    @Override
    public void setGameOptionsMenu(IGameOptionsMenu menu)
    {
        gameOptionsMenu = menu;
    }

    @Override
    public IGameOptionsMenu getGameOptionsMenu()
    {
        return gameOptionsMenu;
    }


}
