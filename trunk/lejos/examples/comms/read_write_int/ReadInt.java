import java.io.*;
import josx.rcxcomm.*;

/**
 * This program uses the josx.rcxcomm package to
 * communicate over the Lego IR link with a
 * corresponding program, WriteInt.
 * It receives int values and sends replies
 * of double the value. It works on the
 * PC and demonstrates the PC initiating communication.
 * It demonstrates the use of DataInputStream
 * and DataOutputStream.
 */
public class ReadInt {

  public static void main(String[] args) {

    try {
	  	if(args.length!=1)
			throw new Exception("first argument must be tower port (USB,COM1 etc)");
      RCXPort port = new RCXPort(args[0]);

      InputStream is = port.getInputStream();
      OutputStream os = port.getOutputStream();
      DataInputStream dis = new DataInputStream(is);
      DataOutputStream dos = new DataOutputStream(os);

      while (true) {

        int n = dis.readInt();

	System.out.println("sending " + (n*2) + " to the RCX");
        dos.writeInt(n*2);
        dos.flush();

      }
    }     
    catch (Exception e) {
    	e.printStackTrace();
    }
  }
}
