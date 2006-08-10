import java.io.*;
import josx.rcxcomm.*;
import josx.platform.rcx.*;

/**
  * This is an RCX class that reads a sensor value and returns the value   
  * to the PC via the IR tower.   
  * @author Brian Bagnall   
 */
public class LNPAddrSensorReader {
  
  public static void main(String args[]) {
    int sensorID, sensorValue;
    RCXLNPAddressingPort port = null;
    try {
      port = new RCXLNPAddressingPort((byte) 0x00, (byte) 0x20);
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
      port.close();
    }
  }
}


