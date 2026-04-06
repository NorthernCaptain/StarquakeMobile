package northern.captain.gamecore.exception;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 05.02.14
 * Time: 22:42
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */

/**
 * This exception is thrown if we need to stop current thread from within the logic
 * It is not derived from LogicException but from Runtime exception
 */
public class LogicExceptionStopThread extends RuntimeException
{
}
