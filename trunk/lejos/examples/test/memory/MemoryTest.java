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
  	LCD.showNumber(1234);
  	Thread.sleep(1000);
  	
  	// Display the number of runs.
  	PersistentMemoryArea pMem = PersistentMemoryArea.get(0xcafe, 1);  	
  	LCD.showNumber(4321);
  	Thread.sleep(1000);
  	
  	pMem.writeByte(0, (byte)(pMem.readByte(0)+1));
  	LCD.showNumber(pMem.readByte(0));
  	Thread.sleep(2000);
  	
    do
    {
      String s = "Some text";
      LCD.showNumber( (int)(Runtime.getRuntime().freeMemory()) / 10);
      Thread.sleep(10);
    } while (true);
  }
}
