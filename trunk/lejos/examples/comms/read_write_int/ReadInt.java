import java.io.*;
import josx.rcxcomm.*;

/**
 * This program uses the josx.rcxcomm package to
 * communicate over the Lego IR link with a
 * corresponding program, WriteInt.
 * It receives int values and sends replies
 * of double the value. It works on either the
 * PC or the  RCX, and so demonsrates either
 * the PC or the RCX initiating communication.
 * It demonstrates the use of DataInputStream
 * and DataOutputStream.
 */
public class ReadInt {

  public static void main(String[] args) {

    RCXPort port = new RCXPort();

    InputStream is = port.getInputStream();
    OutputStream os = port.getOutputStream();
    DataInputStream dis = new DataInputStream(is);
    DataOutputStream dos = new DataOutputStream(os);

    while (true) {
      try {

        int n = dis.readInt();

        dos.writeInt(n*2);
        dos.flush();

      }
      catch (Exception e) {
      }
    }
  }
}
