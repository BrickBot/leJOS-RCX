import josx.rcxcomm.*;
import java.io.*;

/**  Runs on the PC and communicates with Write.java RCX example.
 * Compile with javac with pcrcxcomm.jar on the CLASSPATH
 */
public class Read {
  public static void main(String [] args) throws IOException {
    RCXPort port = new RCXPort();
    InputStream in = port.getInputStream();
    int b = in.read();
    System.out.println("Read: " + b);
  }
}

