import java.io.*;
import josx.rcxcomm.*;

/**
 * This program uses the josx.rcxcomm package to
 * communicate over the Lego IR link with a
 * corresponding program, ReadInt. It sends
 * int values and receives replies of double the
 * value, 5 times. It works on either the
 * PC or the  RCX, and so demonsrates either
 * the PC or the RCX initiating communication.
 * It demonstrates the use of DataInputStream
 * and DataOutputStream.
 */
public class ReadSensor {

  public static void main(String[] args) {

    try {

      RCXPort port = new RCXPort();

      InputStream is = port.getInputStream();
      OutputStream os = port.getOutputStream();
      DataInputStream dis = new DataInputStream(is);
      DataOutputStream dos = new DataOutputStream(os);

      System.out.println("Reading Light Sensor");

      dos.writeByte(1);
      dos.flush();

      int n = dis.readShort();

      System.out.println("Received " + n);

    }
    catch (Exception e) {
      System.out.println("Exception " + e.getMessage());
    }
  }
}
