import java.io.*;
import josx.rcxcomm.*;
import josx.platform.rcx.*;

/**
 * @author LEGO3 Team at DTU-IAU
 */
public class SensorReader {
  
  public static void main(String args[]) {
    int sensorID, sensorValue;
    RCXPort port = null;
    try {
      port = new RCXPort();
      while (true) {
        sensorID = port.getInputStream().read();
        sensorValue = Sensor.readSensorValue(sensorID, 0);
        try { // We have to wait because of a bug in the communication.
          Thread.sleep(100);
        } catch (InterruptedException iE) { }
        LCD.showNumber(sensorValue);
        port.getOutputStream().write(sensorValue/256);
        port.getOutputStream().write(sensorValue%256);
        port.getOutputStream().flush();
      }
    } catch (IOException ioE) {
      LCD.showNumber(1111);
    } finally {
      port.close();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException iE) { }
    }
  }

}

