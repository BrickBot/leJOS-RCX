import java.lang.System;
import josx.platform.rcx.*;

/**
 * Slowly fill memory.
 *
 */
public class MemoryTest
{
  public static void main (String[] arg)
	throws InterruptedException
  {
    do
    {
      String s = "Some text";
      LCD.showNumber( (int)(Runtime.getRuntime().freeMemory()) / 10);
      Thread.sleep(10);
    } while (true);
  }
}
