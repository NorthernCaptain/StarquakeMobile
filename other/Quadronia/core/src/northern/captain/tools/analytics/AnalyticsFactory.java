package northern.captain.tools.analytics;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 01.05.14
 * Time: 23:33
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public abstract class AnalyticsFactory
{
    protected static AnalyticsFactory singleton = null;

    public static void setSingleton(AnalyticsFactory newSingleton)
    {
        //Do not initialize second time, only once per Application
        //required by GoogleAnalytics
        if(singleton == null)
            singleton = newSingleton;
    }

    public static AnalyticsFactory instance()
    {
        return singleton;
    }


    public abstract IAnalytics getAnalytics();
}
