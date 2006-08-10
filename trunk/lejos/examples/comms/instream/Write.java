import josx.rcxcomm.*;

import java.io.*;

/** For use on the PC for sending a byte to the RCX Read.java example
 * Compile using javac with pcrcxcomm.jar on the CLASSPATH
 */
public class Write {
  public static void main(String [] args) throws IOException {
	  try {
		  	if(args.length!=1)
				throw new Exception("first argument must be tower port (USB,COM1 etc)");
		    RCXPort port = new RCXPort(args[0]);
		    OutputStream out = port.getOutputStream();
		    out.write(123);
		  } catch(Exception exc) {
			  exc.printStackTrace();
		  }
  }
}

