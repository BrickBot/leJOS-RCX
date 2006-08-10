import java.io.*;
import josx.rcxcomm.*;
import josx.platform.rcx.*;

/**
  * This is an RCX class that reads a sensor value and returns the value   
  * to the PC via the IR tower.   
  * @author Brian Bagnall   
 */
public class LNPSensorReader {
  
  public static void main(String args[]) {
    int sensorID, sensorValue;
    RCXLNPPort port = null;
    try {
//	  	if(args.length!=1)
//			throw new Exception("first argument must be tower port (USB,COM1 etc)");
      port = new RCXLNPPort();
      DataOutputStream out = new DataOutputStream(port.getOutputStream());   
      while (true) {
        sensorID = port.getInputStream().read();
        sensorValue = Sensor.readSensorValue(sensorID, 0);
        LCD.showNumber(sensorValue);
        out.writeShort(sensorValue);
        out.flush();
      }
    } catch (IOException ioE) {
    	LCD.showNumber(1111);
    } finally {
    	if(port!=null)
    		port.close();
    }
  }
}


