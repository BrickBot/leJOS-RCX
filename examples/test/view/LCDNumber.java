import josx.platform.rcx.LCD;

public class LCDNumber
{
  private LCDNumber()
  {
  }

  public static void set( int value, int decimals, boolean signed)
  {
    LCD.setNumber ( signed ? 0x3001 : 0x301f, value, 0x3002 + decimals);
  }
}
