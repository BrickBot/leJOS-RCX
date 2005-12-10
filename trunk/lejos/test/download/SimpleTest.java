import josx.platform.rcx.*;

public class SimpleTest
{
  public static void main (String[] args) throws Exception
  {
     LCD.clear();
     TextLCD.print ("Simple");
     Thread.sleep(1000);
     TextLCD.print ("Test");
     Thread.sleep(1000);
  }
}
