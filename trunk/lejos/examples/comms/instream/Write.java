import josx.rcxcomm.*;
import java.io.*;

/** For use on the PC for sending a byte to the RCX Read.java example
 * Compile using javac with pcrcxcomm.jar on the CLASSPATH
 */
public class Write {
  public static void main(String [] args) throws IOException {
    RCXPort port = new RCXPort();
    OutputStream out = port.getOutputStream();
    out.write(123);
  }
}

