import java.io.*;

import josx.rcxcomm.*;
import josx.platform.rcx.*;

/**
 * This program uses the josx.rcxcomm package to
 * communicate over the Lego IR link with a
 * corresponding program, ReadInt. It sends
 * int values and receives replies of double the
 * value, 5 times. It works on the RCX and demonstrates 
 * the RCX initiating communication.
 * It demonstrates the use of DataInputStream
 * and DataOutputStream.
 */
public class WriteInt {

  public static void main(String[] args) {

    RCXPort port = null;
    
    try {

      port = new RCXPort();
      
      InputStream is = port.getInputStream();
      OutputStream os = port.getOutputStream();
      DataInputStream dis = new DataInputStream(is);
      DataOutputStream dos = new DataOutputStream(os);

      for(int i=0;i<5;i++) {

        dos.writeInt(i);
        dos.flush();

        int n = dis.readInt();
      }
      Thread.sleep(1000);
	  } catch (Exception ioE) {
	  		LCD.showNumber(1111);
	  } finally {
		  if(port!=null)
			  port.close();
	  }
  }
}
