import josx.platform.rcx.*;

public class SensorView extends PortView implements SensorConstants, Segment
{
  public static final SensorView S1 =
    new SensorView( Sensor.S1, 0);
  public static final SensorView S2 =
    new SensorView( Sensor.S2, 1);
  public static final SensorView S3 =
    new SensorView( Sensor.S3, 2);

  static final int MAXMODE = 7;
  static final int MAXSTATE = 1;

  Sensor sensor;
  int iMode;
  int iState;

  SensorView( Sensor s, int n )
  {
    super( false, n);
    sensor = s;

    iMode = 0;
    setMode();

    iState = 0;
    setState();
  }

  void setMode()
  {
    sensor.setPreviousValue (0);
    if (iMode == 0)
      sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_RAW);
    else if (iMode == 1)
      sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_BOOL);
    else if (iMode == 2)
      sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_EDGE);
    else if (iMode == 3)
      sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_PULSE);
    else if (iMode == 4)
      sensor.setTypeAndMode (SENSOR_TYPE_LIGHT, SENSOR_MODE_PCT);
    else if (iMode == 5)
      sensor.setTypeAndMode (SENSOR_TYPE_ROT, SENSOR_MODE_ANGLE);
    else if (iMode == 6)
      sensor.setTypeAndMode (SENSOR_TYPE_TEMP, SENSOR_MODE_DEGC);
    else if (iMode == 7)
      sensor.setTypeAndMode (SENSOR_TYPE_TEMP, SENSOR_MODE_DEGF);
  }

  void setState()
  {
    if (iState == 0)
      sensor.passivate();
    else if (iState == 1)
      sensor.activate();
  }

  public void showPort()
  {
    if( sensor.readBooleanValue())
      LCD.setSegment( LCDSegment.sensorActive( number));
  }

  public void showValues()
  {
    LCDProgramNumber.set (iMode);
    LCDNumber.set( sensor.readValue(), iMode/6, (iMode != 0));
    if( iState == 0)
      LCD.setSegment( STANDING);
    else if( iState == 1)
      LCD.setSegment( WALKING);
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
    iMode++;
    if (iMode > MAXMODE)
      iMode = 0;
    setMode();
  }

  public void shutdown()
  {
    sensor.passivate();
  }
}
