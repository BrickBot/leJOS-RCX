package josx.rcxcomm;

import java.net.*;
import java.io.*;

/**
 *
 * A Proxy for sending HTTP requests to the RCX.
 *
 * Listens on the port specified by -port <port> (default 80).
 * If the RCX is currently servicing a request, it returns the massge "RCX is Busy".
 *
 * The constructor does not return - it runs forever.
 *
 * @author 	Lawrie Griffiths
 *
 */
public class HttpProxy {
  private boolean busy = false;

  public HttpProxy(int port) throws IOException {

    ServerSocket serverSock = new ServerSocket(port);

    while (true) {
      System.out.println("Waiting for request on port " + port);
      Socket sock = serverSock.accept();
      System.out.println("Received request");
      if (busy) {
        System.out.println("RCX is busy");
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        String s = in.readLine();
        PrintWriter pw = new PrintWriter(sock.getOutputStream());
        pw.println("HTTP/1.0 200");
        pw.println("Content-Type: text/html");
        pw.println();
        pw.println("<HTML><BODY>RCX is Busy</BODY></HTML>");
        pw.close();
        // sock.close();
      } else {
        setBusy(true);
        RCXThread rcxThread = new RCXThread(this,sock);
        rcxThread.start();
      }
    }
  }

  /**
   * Called by RCXThread when the RCX has processed a request
   **/
  synchronized void setBusy(boolean busy) {
   this.busy = busy;
  }

  public static void main(String [] args) throws IOException {
    int port=80;

    for(int i=0;i<args.length;i++) {
      if (args[i].equals("-port")) {
        if (i <args.length-1) port = Integer.parseInt(args[i+1]);
        i++;
      }
    }

    // Runs forever

    HttpProxy proxy = new HttpProxy(port);
  }

  /**
   * Innner class to deal with a single request
   */
  class RCXThread extends Thread {
    private Socket sock;  
    private HttpProxy proxy;

    RCXThread(HttpProxy proxy, Socket sock) {
      this.sock = sock;
      this.proxy = proxy;
    }

    public void run() {
      try {

        // Open the RCXPort and get the port and socket streams

        RCXPort port = new RCXPort();
        InputStream is = port.getInputStream();
        OutputStream os = port.getOutputStream();

        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream();
    
        byte b = 0, lastB = 0, lastB2 = 0, lastB3 = 0;
        int inb;

        // Write the Get

        do {
          lastB = b;
          b = (byte) in.read();
          System.out.println("Input Stream: " + (char) b);
          os.write(b);
        } while (!(b == '\n' && lastB == '\r'));

        os.write((byte) '\r');
        os.write((byte) '\n');

        // Skip the headers

        System.out.println("Skipping headers");

        do {
          lastB3 = lastB2;
          lastB2 = lastB;
          lastB = b;
          b = (byte) in.read();
        } while (!(b == '\n' && lastB == '\r' && lastB2 == '\n' && lastB3 == '\r'));

        // Read the reply

        do {
          inb = is.read();
          if (inb == 0xff) {
            out.flush();
            sock.close();
            System.out.println("Socket closed");
          } else {
            System.out.println("Output Stream: " + (char) inb);
            out.write(inb);
          }
        } while (inb != 0xff);

        port.close();
        proxy.setBusy(false);
      }
      catch (IOException ioe) {}
    }
  }
}

