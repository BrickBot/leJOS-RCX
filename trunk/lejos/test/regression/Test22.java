import tinyvm.rcx.*;

public class Test22
{
  public static void main (String[] arg)
  {
    Sensor.S2.addSensorListener (new SensorListener() {
      public void stateChanged (Sensor src, boolean value) {
        if (value)
          Sound.beep();
      }
      public void stateChanged (Sensor src, int value) {
        LCD.showNumber (value);
        for (int k = 0; k < 10; k++) { }
      }
    });
    Sensor.S2.activate();
  }
}
