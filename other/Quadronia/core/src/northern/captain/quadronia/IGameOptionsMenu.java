package northern.captain.quadronia;

import northern.captain.tools.IPersistCfg;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 02.04.13
 * Time: 0:13
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public interface IGameOptionsMenu extends IPersistCfg
{
    void setLang(int idx);

    void setLang();

    String getAppLang();

    void showLangChooser();

    void flipSoundOption();

    void flipVibroOption();

    void flipStatusBarOption();

    void flipMusinOption();

    void flipTutorial();

    boolean isSoundOn();

    boolean isVibrationOn();

    boolean isStatusBarOn();

    boolean isMusicOn();

    boolean isTutorialOn();

    public void openFeedback();

    int getThemeIndex();
    void setThemeIndex(int newIdx);
}
