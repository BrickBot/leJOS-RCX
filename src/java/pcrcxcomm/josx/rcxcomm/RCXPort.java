package josx.rcxcomm;

import java.io.*;
import josx.rcxcomm.Tower;

/** A DataPort designed for use on a computer with a serial or a USB-connected
 * IR Tower. The USB implementation only currently works for Windows.
 * Adapted from original code made by the LEGO3 Team at DTU-IAU
 * @author Brian Bagnall
 * @author Lawrie Griffiths
 */
public class RCXPort extends DataPort {

  private Tower tower;
  int bytesRead;
  boolean open = false;
  int usbFlag = 0;
  int sendTime = 0;

  /** Creates new RCXPort
  * @param port. Specifies the port that the IR Tower is connected to.
  */
  public RCXPort(String port) throws IOException {
    super();
    open(port.toUpperCase());
  }

  /** Creates new RCXPort
   *  the port is obtained from RCXTTY, defaulting to usb
  */
  public RCXPort() throws IOException {
    super();
    open("");
  }

  private void open(String port) throws IOException {
    tower = new Tower();
    int r = tower.open(port);

    // Throw an IO Exception if the Open fails
    if (r < 0)
      throw(new IOException("Open " + port + " failed with message: "
                            + tower.strerror(-r) + ", error: "
                            + tower.getError()));
    else open = true;
    usbFlag = tower.getUsbFlag();
  }

  private byte[] buffer = new byte[9];
  private static byte[] keepAlive= {0x00}; 
  private static byte [] trash = new byte[1];

  protected boolean dataAvailable() {
    if (bytesRead == 9) return true;

    // Due the the way that DataPort and RCXPort currently interact,
    // dataAvailable can be called before the port is open.
    if (tower == null || !open) return false;

    // Read 9 bytes into the buffer
    bytesRead = tower.read(buffer);

    // If its a serial tower and we are in listen mode,
    // send a keep alive byte every 5 seconds

    if (usbFlag == 0 && listen && bytesRead == 0) {
      int currTime = (int)System.currentTimeMillis();
      if ((currTime - sendTime) >= 5000) {
        tower.write(keepAlive,1);
        tower.read(trash); // discard
        sendTime = currTime;
      }
    }

    return (bytesRead == 9);
  }

  private byte[] packet = {(byte)0xf7, (byte)0x00};

  protected void sendByte(byte b) throws IOException {
    packet[1] = b;

    sendTime = (int)System.currentTimeMillis();

    int actual = tower.send(packet,packet.length);
    // tower.hexdump("Send",packet,packet.length);
    if (actual != 2) {
      throw new IOException("Failed to send whole packet");
    }
  }

  protected byte receiveByte() throws IOException {
    if (bytesRead == 0) throw new IOException("dataAvailable not called");
    if (bytesRead < 9) throw new IOException("Insufficient data");
    bytesRead = 0;

    if (buffer[0]==(byte)0x55 && buffer[1]==(byte)0xff && buffer[2]==(byte)0x00)
     if (buffer[3] == ~buffer[4] && buffer[3] == (byte)0xf7)
        if (buffer[5] == ~buffer[6]) {
           return buffer[5];
        }
    throw new IOException("Something wrong in the package.");
  }

  /** Close the RCXPort.
  * The port can not be reopened.
  */
  public void close(){
    super.close();
    int r = tower.close();
    open = false;
  }

  public void setListen(boolean b) {
    listen = b;
  }
}




