package northern.captain.gamecore.android.gplus;

import northern.captain.gamecore.gplus.GoogleGamesFactory;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 10.05.14
 * Time: 21:24
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class GoogleGamesFactoryAnd extends GoogleGamesFactory
{
    public static void initialize()
    {
        singleton = new GoogleGamesFactoryAnd();
    }


    public GoogleGamesFactoryAnd()
    {
        this.processor = new GoogleGamesProcessor();
    }
}
