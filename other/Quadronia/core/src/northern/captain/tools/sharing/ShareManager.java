package northern.captain.tools.sharing;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 05.05.14
 * Time: 23:57
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class ShareManager
{
    protected static ShareManager singleton = null;

    public static ShareManager instance()
    {
        return singleton;
    }

    public static void setSingleton(ShareManager manager)
    {
        singleton = manager;
    }

    public void doShareMyProgress(int mode)
    {

    }
}
