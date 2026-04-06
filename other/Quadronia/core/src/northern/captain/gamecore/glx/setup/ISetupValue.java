package northern.captain.gamecore.glx.setup;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 12.09.13
 * Time: 23:22
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */

/**
 * Interface for accessing and setting setup values from SetupVisual
 */
public interface ISetupValue
{
    public boolean getBValue();

    public void setBValue(boolean newValue, SetupVisual visual);

    public void activateAction(SetupVisual visual);

}
