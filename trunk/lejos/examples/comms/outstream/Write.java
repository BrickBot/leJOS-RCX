import josx.rcxcomm.*;
import java.io.*;

/** Very simple example of using RCXOutputStream
 * Communicates with Read.java running on the PC.
 * For 2-way communication use RCXPort.
 */
public class Write {
  public static void main(String [] args) throws IOException {
    RCXOutputStream out = new RCXOutputStream();
    out.write(123);
  }
}

