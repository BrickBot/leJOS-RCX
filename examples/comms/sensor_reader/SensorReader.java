import java.io.*;
import josx.rcxcomm.*;
import josx.platform.rcx.*;

/**
  * This is an RCX class that reads a sensor value and returns the value   
  * to the PC via the IR tower.   
  * @author Brian Bagnall   
 */
public class SensorReader {
  public static void main(String args[]) {
    int sensorID, sensorValue;
    RCXPort port = null;
    try {
      port = new RCXPort();
      DataOutputStream out = new DataOutputStream(port.getOutputStream());   
      while (true) {
        port.reset(); // As PC closes port for each message
        sensorID = port.getInputStream().read();
        sensorValue = Sensor.readSensorValue(sensorID, 0);
        LCD.showNumber(sensorValue);
        out.writeInt(sensorValue);          
        out.flush();
      }
    } catch (Exception ioE) {
    	LCD.showNumber(1111);   
    } finally {
    	if(port!=null)
    		port.close();
    }
  }
}

