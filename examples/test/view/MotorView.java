import josx.platform.rcx.*;

public class MotorView extends PortView
{
  static final int MAXSTATE = 3;
  static final int MAXPOWER = 7;

  public static final MotorView A = new MotorView( Motor.A, 0);
  public static final MotorView B = new MotorView( Motor.B, 1);
  public static final MotorView C = new MotorView( Motor.C, 2);

  final Motor motor;

  int iState;
  int iPower;

  MotorView( Motor m, int n )
  {
    super( true, n);
    motor = m;

    iState = 0;
    iPower = 7;
    setState();
    setPower();
  }

  void setPower()
  {
    motor.setPower(iPower);
  }

  void setState()
  {
    if (iState == 0)
      motor.flt();
    else if (iState == 1)
      motor.forward();
    else if (iState == 2)
      motor.backward();
    else if (iState == 3)
      motor.stop();
  }

  public void showPort()
  {
    if (iState == 0)
      {}
    else if (iState == 1)
      LCD.setSegment( LCDSegment.motorForward( number));
    else if (iState == 2)
      LCD.setSegment( LCDSegment.motorBackward( number));
    else if (iState == 3){
      LCD.setSegment( LCDSegment.motorForward( number));
      LCD.setSegment( LCDSegment.motorBackward( number));
    }
  }

  public void showValues()
  {
    LCDProgramNumber.set (iState);
    LCDNumber.set (iPower, 0, true);
  }

  public void runPressed()
  {
    iState++;
    if (iState > MAXSTATE)
      iState = 0;
    setState();
  }

  public void prgmPressed()
  {
    iPower++;
    if (iPower > MAXPOWER)
      iPower = 0;
    setPower();
  }

  public void shutdown()
  {
    motor.stop();
  }
}
