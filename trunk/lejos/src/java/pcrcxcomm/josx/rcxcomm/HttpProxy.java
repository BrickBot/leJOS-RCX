package josx.rcxcomm;

import java.net.*;
import java.io.*;

public class HttpProxy {
  boolean busy = false;

  public HttpProxy(boolean start, int port) throws IOException {

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
        RCXThread rcxThread = new RCXThread(this,sock,start);
        rcxThread.start();
      }
    }
  }

  public synchronized void setBusy(boolean busy) {
    this.busy = busy;
  }

  public static void main(String [] args) throws IOException {
    boolean start = false;
    int port=80;

    for(int i=0;i<args.length;i++) {
      if (args[i].equals("-start")) start = true;
      else if (args[i].equals("-port")) {
        if (i <args.length-1) port = Integer.parseInt(args[i+1]);
        i++;
      }
    }

    HttpProxy proxy = new HttpProxy(start,port);
  }

  class RCXThread extends Thread {
    private Socket sock;  
    private HttpProxy proxy;
    private boolean start;

    public RCXThread(HttpProxy proxy,Socket sock, boolean start) {
      this.sock = sock;
      this.proxy = proxy;
      this.start = start;
    }

    public void run() {

      if (start) {
        System.out.println("Starting RCX program");
        Tower tower = new Tower();
        tower.open();
        byte [] start = {(byte)0xd2,0x02,0x00};
        tower.send(start,3);
        tower.close();
      }

      try {
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

