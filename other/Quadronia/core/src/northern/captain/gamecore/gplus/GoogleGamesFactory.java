package northern.captain.gamecore.gplus;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 10.05.14
 * Time: 21:21
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class GoogleGamesFactory
{
    protected static GoogleGamesFactory singleton = null;
    public static GoogleGamesFactory instance()
    {
        return singleton;
    }

    protected IGoogleGamesProcessor processor;

    public IGoogleGamesProcessor getProcessor()
    {
        return processor;
    }

    public static void initialize()
    {
        singleton = new GoogleGamesFactory();
    }

    public GoogleGamesFactory()
    {
        processor = new FakeGoogleGamesProcessor();
    }

}
