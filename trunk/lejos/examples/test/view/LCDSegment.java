import josx.platform.rcx.Segment;

public class LCDSegment implements Segment
{
  private static final int[] MOTOR_VIEW = 
    new int[] { MOTOR_A_VIEW, MOTOR_B_VIEW, MOTOR_C_VIEW};
  private static final int[] MOTOR_FORWARD = 
    new int[] { MOTOR_A_FWD, MOTOR_B_FWD, MOTOR_C_FWD};
  private static final int[] MOTOR_BACKWARD = 
    new int[] { MOTOR_A_REV, MOTOR_B_REV, MOTOR_C_REV};

  private static final int[] SENSOR_VIEW = 
    new int[] { SENSOR_1_VIEW, SENSOR_2_VIEW, SENSOR_3_VIEW};
  private static final int[] SENSOR_ACTIVE = 
    new int[] { SENSOR_1_ACTIVE, SENSOR_2_ACTIVE, SENSOR_3_ACTIVE};

  private LCDSegment()
  {
  }

  public static int view( boolean motor, int n)
  {
    return( motor ? MOTOR_VIEW[n] : SENSOR_VIEW[n] );
  }

  public static int sensorActive( int n)
  {
    return( SENSOR_ACTIVE[n] );
  }

  public static int motorForward( int n)
  {
    return( MOTOR_FORWARD[n] );
  }

  public static int motorBackward( int n)
  {
    return( MOTOR_BACKWARD[n] );
  }
}
