import java.io.*;
import josx.rcxcomm.*;

/** Very simple example of using RCXInputStream on the RCX
 * Communicates with Write.java on the PC
 * For 2-way communication use RCXPort
 */
public class Read {
  public static void main (String [] args) throws IOException {
    RCXInputStream in = new RCXInputStream();
    in.read();
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException e) {}
  }
}

