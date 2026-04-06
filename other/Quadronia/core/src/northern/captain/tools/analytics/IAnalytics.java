package northern.captain.tools.analytics;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 01.05.14
 * Time: 22:43
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public interface IAnalytics
{
    /**
     * Registers that the user enters the screen with the given name
     * @param screenName
     */
    void registerScreenEnter(String screenName);

    /**
     * Register button clicked action for the given button
     *
     * @param buttonName - name of the button clicked in "ScreenName.ButtonName" format
     */
    void registerButtonAction(String buttonName);


    /**
     * Register inner sale operation for the given sku
     *
     * @param SKUName
     * @param amount
     */
    void registerInnerSale(String SKUName, int amount);

    /**
     * Register usage of particular weapon
     * @param weaponName
     * @param qty
     */
    void registerWeaponUsage(String weaponName, int qty);

    /**
     * Register start of the game with the given type
     * @param gameType
     */
    void registerGameStart(String gameType);


    enum GAME_FINISH
    {
        WIN_OK,
        WIN_SUR,
        LOST_OK,
        LOST_SUR,
        TERMINATE
    }

    /**
     * Register finish of the game with the given type and result code
     * @param gameType
     * @param finish
     */
    void registerGameFinish(String gameType, GAME_FINISH finish);

    /**
     * Registers internal exception as event with category exception
     * @param action
     * @param exception
     */
    void registerException(String action, Throwable exception);

}
