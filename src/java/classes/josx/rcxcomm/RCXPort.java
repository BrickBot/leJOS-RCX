package josx.rcxcomm;

import java.io.*;
import josx.platform.rcx.*;

/** Creates a new <code>RCXDataPort</code>
 * Adapted from original code made by the LEGO3 Team at DTU-IAU
 * @author Brian Bagnall
 */
public class RCXPort extends DataPort {
  
  protected boolean dataAvailable() {
    return josx.platform.rcx.Serial.isPacketAvailable();
  }
  
  private byte[] packet = {(byte)0xf7, (byte)0x00};
  protected void sendByte(byte b) throws IOException {
    packet[1] = b;
    josx.platform.rcx.Serial.sendPacket(packet, 0, 2);
  }
  
  private static byte[] buffer = new byte[10];
  protected byte receiveByte() throws IOException {
    josx.platform.rcx.Serial.readPacket(buffer);
    return buffer[1];
  }  
}

