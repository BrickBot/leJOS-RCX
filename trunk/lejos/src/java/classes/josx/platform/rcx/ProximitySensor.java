package josx.platform.rcx;

/**
 * A 'sensor' to detect object proximity. Sends a short
 * message out of the infra-red port every so often and looks for
 * the reflection using the light sensor (which happens to be
 * very infra-red sensitive).
 *
 * @author Paul Andrews
 */
public class ProximitySensor implements SensorListener {
    static final byte[] data = { -1 };
    Sensor sensor;
    int threshhold;
    
    /**
     * Constructor. Threshhold defaults to 15.
     * @param sensor the sensor to which the light detector is connected.
     * the constructor will initialize the sensor and set up the infrared
     * transmitter.
     */
    public ProximitySensor(Sensor sensor) {
        this(sensor, 15);
    }

    /**
     * Constructor.
     * @param sensor the sensor to which the light detector is connected.
     * the constructor will initialize the sensor and set up the infrared
     * transmitter.
     * @param threshhold the bigger the number the closer we get.
     */
    public ProximitySensor(Sensor sensor, int threshhold) {
        this.sensor = sensor;
        sensor.setTypeAndMode(SensorConstants.SENSOR_TYPE_LIGHT, SensorConstants.SENSOR_MODE_PCT);
        sensor.activate();
        Serial.setRangeLong();
        this.threshhold = threshhold;
        sensor.addSensorListener(this);
        new Emitter().start();
    }

    /**
     * Block the current thread until a near object is detected.
     * A user could simply do their own sensor.wait() as that is
     * all this method does.
     *
     * @param millis wait at most millis milliseconds. 0 means wait forever
     * @throws InterruptedException if some thread calls interrupt() on the
     * calling thread.
     */
    public void waitTillNear(long millis) throws InterruptedException
    {
        synchronized (sensor) {
            sensor.wait(millis);
        }
    }
    
    /**
     * Called from a thread private to sensor that runs at MAX_PRIORITY
     * If newValue > oldValue by more than the threshhold, notify anything
     * wait()ing on the sensor.
     */
    public void stateChanged(Sensor sensor, int oldValue, int newValue) {
        if (newValue - oldValue > threshhold)
            synchronized(sensor) {
                sensor.notifyAll();
            }
    }
    
    /**
     * A thread to continuously send out infrared pulses. Each
     * pulse lasts about 40ms and the thread waits for 100ms between
     * pulses. Thread runs at MAX_PRIORITY to guarantee that it runs.
     */
    class Emitter extends Thread
    {
        public Emitter()
        {
            setDaemon(true);
            setPriority(Thread.MAX_PRIORITY);
        }
               
        public void run()
        {
            while (true)
            {
                Serial.sendPacket(data, 0, 1);
                try {
                    sleep(100);
                } catch (InterruptedException ie) {
                }
            }
        }
    }
}
