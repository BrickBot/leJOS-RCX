package josx.platform.rcx;

/**
 * Implmentation of a servo using a Motor and a Rotation Sensor.
 *
 * @author Paul Andrews
 */
public class Servo implements SensorListener
{
    Sensor sensor;
    Motor motor;
    int position;
    int targetPosition;
    int slack;

    /**
     * Constructor. Initializes the sensor and sets the current
     * postion to zero.
     * @param sensor the Sensor to which the rotation sensor is connected.
     * @param motor the Motor to control.
     * @param slack accept positions +/- this value.
     */    
    public Servo(Sensor sensor, Motor motor, int slack)
    {
        this.sensor= sensor;
        this.motor = motor;
        this.slack = slack;
        targetPosition = 0;
        position = 0;
        sensor.addSensorListener(this);
        sensor.setTypeAndMode(SensorConstants.SENSOR_TYPE_ROT, SensorConstants.SENSOR_MODE_ANGLE);
        sensor.setPreviousValue(0);
        sensor.activate();
    }

    /**
     * Constructor. Same as Servo(sensor, motor, 0);
     */
    public Servo(Sensor sensor, Motor motor)
    {
        this(sensor, motor, 0);
    }

    /**
     * Set the motor rotating to the specified position.
     * The method returns immediately.
     *
     * @param pos the position to rotate to. The effect will depend
     * on the gearing used between the motor and the sensor and the
     * motor and the wheels (or whatever).
     * @return true if the servo is already at the specified
     * position. False otherwise.
     */
    public boolean rotateTo(int pos)
    {
        synchronized (this)
        {
            boolean ret = false;
            this.targetPosition = pos;
            int diff = targetPosition - position;
            motor.setPower(7);
            if (diff < slack)
                motor.forward();
            else if (diff > slack)
                motor.backward();
            else
                ret = true;
                
            return ret;
        }
    }

    /**
     * Called by the sensor listener thread when the value of the
     * rotation sensor changes. Once the motor has rotated to the
     * desired value, +/- slack, stop it and notify anyone waiting
     * on this object's monitor.
     */        
    public void stateChanged(Sensor sensor, int oldValue, int newValue)
    {
        synchronized (this)
        {
            position = newValue;
            int diff = targetPosition - position;
            if (diff >= -slack && diff <= slack)
            {
                motor.stop();
                notifyAll();
            }
        }
    }
}