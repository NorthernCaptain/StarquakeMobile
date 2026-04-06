package northern.captain.tools;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 19.08.13
 * Time: 22:15
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class StringUtils
{
    static public boolean isNullOrEmpty(String value)
    {
        return value == null || value.length() == 0;
    }

    static public String shorten(String value, int toLen)
    {
        if(value.length() >= toLen)
        {
            return value.substring(0, toLen - 3) + "...";
        }
        return value;
    }
}
