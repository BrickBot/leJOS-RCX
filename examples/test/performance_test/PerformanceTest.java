import java.lang.System;
import josx.platform.rcx.*;

/**
 * This program tests the performance with and without Listeners,
 * speed as well as memory efficiency.
 * Press Run repeatedly to obtain 8 values
 * (the number of the value is shown in the program number):
 *
 * 1/3/5/7: time in ms for empty for loop of 10000 iterations
 * 2/4/6/8: free memory
 *
 * 1/2: no listeners
 * 3/4: one button listener
 * 5/6: two button listeners
 * 7/8: two button listeners and a sensor listener
 *
 * */

public class PerformanceTest
{
  public static void main (String[] arg)
    throws InterruptedException
  {
    long t0;
    long t1;
    
    t0 = System.currentTimeMillis();
    for( int i=0; i<10000; i++){};
    t1 = System.currentTimeMillis();

    LCD.showProgramNumber( 1);
    LCD.showNumber( (int)t1-(int)t0);
    Button.RUN.waitForPressAndRelease();

    LCD.showProgramNumber( 2);
    LCD.showNumber( (int)(Runtime.getRuntime().freeMemory())-10000);
    Button.RUN.waitForPressAndRelease();

    Button.PRGM.addButtonListener( new ButtonListener() {
        private int count = 0;

        public void buttonPressed( Button button) {
          count++;
          LCD.showProgramNumber( count);
        }
        public void buttonReleased( Button button) {
        }
      }
    );
    t0 = System.currentTimeMillis();
    for( int i=0; i<10000; i++){};
    t1 = System.currentTimeMillis();

    LCD.showProgramNumber( 3);
    LCD.showNumber( (int)t1-(int)t0);
    Button.RUN.waitForPressAndRelease();

    LCD.showProgramNumber( 4);
    LCD.showNumber( (int)(Runtime.getRuntime().freeMemory())-10000);
    Button.RUN.waitForPressAndRelease();

    Button.VIEW.addButtonListener( new ButtonListener() {
        public void buttonPressed( Button button) {
        }
        public void buttonReleased( Button button) {
        }
      }
    );
    t0 = System.currentTimeMillis();
    for( int i=0; i<10000; i++){};
    t1 = System.currentTimeMillis();

    LCD.showProgramNumber( 5);
    LCD.showNumber( (int)t1-(int)t0);
    Button.RUN.waitForPressAndRelease();

    LCD.showProgramNumber( 6);
    LCD.showNumber( (int)(Runtime.getRuntime().freeMemory())-10000);
    Button.RUN.waitForPressAndRelease();

    Sensor.S1.addSensorListener( new SensorListener() {
        public void stateChanged( Sensor sensor, int oldValue, int newValue) {
        }
      }
    );
    t0 = System.currentTimeMillis();
    for( int i=0; i<10000; i++){};
    t1 = System.currentTimeMillis();

    LCD.showProgramNumber( 7);
    LCD.showNumber( (int)t1-(int)t0);
    Button.RUN.waitForPressAndRelease();

    LCD.showProgramNumber( 8);
    LCD.showNumber( (int)(Runtime.getRuntime().freeMemory())-10000);
    Button.RUN.waitForPressAndRelease();

  }
}
