package northern.captain.quadronia.screens;

import northern.captain.gamecore.glx.INCore;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenWorkflow;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 05.06.14
 * Time: 21:58
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class ScreenWorkflowCurve extends ScreenWorkflow
{
    public ScreenWorkflowCurve(INCore app)
    {
        super(app);
    }

    @Override
    protected void initScreenNames()
    {
        super.initScreenNames();

        stateMap.put(stateAct(STATE_INITIAL, WF_ANY), STATE_MAIN_MENU);

        stateMap.put(stateAct(STATE_MAIN_MENU, WF_NEW_GAME), STATE_BATTLE);
        stateMap.put(stateAct(STATE_MAIN_MENU, WF_RESUME_GAME), STATE_BATTLE);
        stateMap.put(stateAct(STATE_MAIN_MENU, WF_OPTIONS), STATE_SCORES); //STATE_SCORES
        stateMap.put(stateAct(STATE_MAIN_MENU, WF_HELP), STATE_HELP); //STATE_HELP!
        stateMap.put(stateAct(STATE_MAIN_MENU, WF_QUIT), STATE_EXIT);
        stateMap.put(stateAct(STATE_MAIN_MENU, WF_BACK), STATE_EXIT);
        stateMap.put(stateAct(STATE_MAIN_MENU, WF_STAY), STATE_MAIN_MENU);
        stateMap.put(stateAct(STATE_MAIN_MENU, WF_CHANGE), STATE_ARMORY_SHOP);

        stateMap.put(stateAct(STATE_BATTLE, WF_BACK), STATE_MAIN_MENU);
        stateMap.put(stateAct(STATE_BATTLE, WF_BACK_AUTO), STATE_MAIN_MENU);
        stateMap.put(stateAct(STATE_BATTLE, WF_CHANGE), STATE_ARMORY_SHOP);

        stateMap.put(stateAct(STATE_ARMORY_SHOP, WF_BACK), STATE_BATTLE);

        addScreenActivator(IScreenWorkflow.STATE_MAIN_MENU,
                new ScreenMainCarousel(this));
        addScreenActivator(IScreenWorkflow.STATE_BATTLE,
                new ScreenGame(this));
        addScreenActivator(IScreenWorkflow.STATE_ARMORY_SHOP,
                new ScreenShopStandalone(this));
    }
}
