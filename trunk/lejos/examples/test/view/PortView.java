import josx.platform.rcx.LCD;

public abstract class PortView
{
  boolean isMotor;
  int number;

  public PortView( boolean m, int n )
  {
    isMotor = m;
    number = n;
  }

  public void showCursor()
  {
    LCD.setSegment( LCDSegment.view( isMotor, number));
  }

  public abstract void runPressed();
  public abstract void prgmPressed();
  public abstract void showPort();
  public abstract void showValues();
  public abstract void shutdown();
}
