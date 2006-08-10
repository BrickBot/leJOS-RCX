import josx.rcxcomm.*;
import java.io.*;

/**  Runs on the PC and communicates with Write.java RCX example.
 * Compile with javac with pcrcxcomm.jar on the CLASSPATH
 */
public class Read {
  public static void main(String [] args) throws IOException {
	  try {
		  	if(args.length!=1)
				throw new Exception("first argument must be tower port (USB,COM1 etc)");
		  	RCXPort port = new RCXPort(args[0]);
		    InputStream in = port.getInputStream();
		    int b = in.read();
		    System.out.println("Read: " + b);
	  } catch(Exception exc) {
		  exc.printStackTrace();
	  }
  }
}

