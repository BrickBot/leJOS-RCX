import josx.platform.rcx.LCD;

public class LCDProgramNumber
{
  private LCDProgramNumber()
  {
  }

  public static void set( int v)
  {
    LCD.setNumber ( 0x3017, v, 0x3002);
  }
}
