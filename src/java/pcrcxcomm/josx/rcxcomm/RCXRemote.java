package josx.rcxcomm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Utility class to support remote execution.
 * Creates an RCXPort for communication.
 * public data stream variables in anfd out are used 
 * to send add receive data. 
 **/
public class RCXRemote {
  private static RCXPort port;
  public static DataInputStream in;
  public static DataOutputStream out;

  static {
    try {
      System.out.println("Starting remoting");
      start();
    } catch (IOException e) {
      System.out.println("Remoting failed");
    }
    System.out.println("Remoting started");
  }

  public static void start() throws IOException {
    port = new RCXPort();
    in = new DataInputStream(port.getInputStream());
    out = new DataOutputStream(port.getOutputStream());
  }

  public static void stop() {
    port.close();
  }

  public static void error() {
    System.out.println("Error in remote execution");
  }
}

