import josx.platform.rcx.*;

public class SensorView implements PortView, SensorConstants, LCDConstants, Segment
{
    public static final SensorView S1 =
	new SensorView( Sensor.S1, 0);
    public static final SensorView S2 =
	new SensorView( Sensor.S2, 1);
    public static final SensorView S3 =
	new SensorView( Sensor.S3, 2);

    static final int MAXMODE = 7;
    static final int MAXSTATE = 1;

    private static final int[] SENSOR_VIEW = 
	new int[] { SENSOR_1_VIEW, SENSOR_2_VIEW, SENSOR_3_VIEW};
    private static final int[] SENSOR_ACTIVE = 
	new int[] { SENSOR_1_ACTIVE, SENSOR_2_ACTIVE, SENSOR_3_ACTIVE};

    Sensor sensor;

    int number;
    int mode;
    int state;

    SensorView( Sensor s, int n )
    {
	sensor = s;
	number = n;

	mode = 0;
	setMode();

	state = 0;
	setState();
    }

    void setMode()
    {
	sensor.setPreviousValue (0);
	if (mode == 0)
	    sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_RAW);
	else if (mode == 1)
	    sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_BOOL);
	else if (mode == 2)
	    sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_EDGE);
	else if (mode == 3)
	    sensor.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_PULSE);
	else if (mode == 4)
	    sensor.setTypeAndMode (SENSOR_TYPE_LIGHT, SENSOR_MODE_PCT);
	else if (mode == 5)
	    sensor.setTypeAndMode (SENSOR_TYPE_ROT, SENSOR_MODE_ANGLE);
	else if (mode == 6)
	    sensor.setTypeAndMode (SENSOR_TYPE_TEMP, SENSOR_MODE_DEGC);
	else if (mode == 7)
	    sensor.setTypeAndMode (SENSOR_TYPE_TEMP, SENSOR_MODE_DEGF);
    }

    void setState()
    {
	if (state == 0)
	    sensor.passivate();
	else if (state == 1)
	    sensor.activate();
    }

    public void showCursor()
    {
	LCD.setSegment (SENSOR_VIEW[number]);
    }

    public void showPort()
    {
	if( sensor.readBooleanValue())
	    LCD.setSegment (SENSOR_ACTIVE[number]);
    }

    public void showValues()
    {
	LCD.setNumber (LCD_PROGRAM, mode, 0);
	if (mode == 0) {	// raw
	    LCD.setNumber (LCD_UNSIGNED, sensor.readValue(), 0);
	} else if (mode <= 5) { // bool, counts, percent, angle
	    LCD.setNumber (LCD_SIGNED, sensor.readValue(), LCD_DECIMAL_0);
	} else {		// temperatures
	    LCD.setNumber (LCD_SIGNED, sensor.readValue(), LCD_DECIMAL_1);
	}

	if( state == 0)
	    LCD.setSegment (STANDING);
	else if( state == 1)
	    LCD.setSegment (WALKING);
    }

    public void runPressed()
    {
	state++;
	if (state > MAXSTATE)
	    state = 0;
	setState();
    }

    public void prgmPressed()
    {
	mode++;
	if (mode > MAXMODE)
	    mode = 0;
	setMode();
    }

    public void shutdown()
    {
	sensor.passivate();
    }
}
