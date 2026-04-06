package northern.captain.tools.analytics;

import northern.captain.gamecore.glx.NCore;
import northern.captain.tools.Log;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 01.05.14
 * Time: 23:29
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class AnalyticsToLog implements IAnalytics
{
    /**
     * Registers that the user enters the screen with the given name
     *
     * @param screenName
     */
    @Override
    public void registerScreenEnter(String screenName)
    {
        Log.i("ncgamean", "Entering screen " + screenName);
    }

    /**
     * Register button clicked action for the given button
     *
     * @param buttonName - name of the button clicked in "ScreenName.ButtonName" format
     */
    @Override
    public void registerButtonAction(String buttonName)
    {
        String fullName = NCore.instance().getScreenFlow().getCurrentScreenName() + "." + buttonName;
        Log.i("ncgamean", "Clicked button " + fullName);
    }

    /**
     * Register inner sale operation for the given sku
     *
     * @param SKUName
     * @param amount
     */
    @Override
    public void registerInnerSale(String SKUName, int amount)
    {
        Log.i("ncgamean", "Inner sale of SKU " + SKUName + ", amount=" + amount);
    }

    /**
     * Register usage of particular weapon
     *
     * @param weaponName
     * @param qty
     */
    @Override
    public void registerWeaponUsage(String weaponName, int qty)
    {
        Log.i("ncgamean", "Weapon used " + weaponName + ", amount=" + qty);
    }

    /**
     * Register start of the game with the given type
     *
     * @param gameType
     */
    @Override
    public void registerGameStart(String gameType)
    {
        Log.i("ncgamean", "Start of the game: " + gameType);
    }

    /**
     * Register finish of the game with the given type and result code
     *
     * @param gameType
     * @param finish
     */
    @Override
    public void registerGameFinish(String gameType, GAME_FINISH finish)
    {
        Log.i("ncgamean", "Finish of the game " + gameType + ", result=" + finish.toString());
    }

    /**
     * Registers internal exception as event with category exception
     *
     * @param action
     * @param exception
     */
    @Override
    public void registerException(String action, Throwable exception)
    {
        Log.i("ncgamean", "Register exception for action " + action + ", type=" + exception.getClass().getSimpleName());
    }
}
