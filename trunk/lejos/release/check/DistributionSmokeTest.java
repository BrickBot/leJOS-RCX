import josx.platform.rcx.*;

public class DistributionSmokeTest
{
  public static void main (String[] args) throws Exception
  {
     LCD.clear();
     TextLCD.print ("SMOKE");
     Thread.sleep(1000);
     TextLCD.print ("TEST");
     Thread.sleep(1000);
  }
}
