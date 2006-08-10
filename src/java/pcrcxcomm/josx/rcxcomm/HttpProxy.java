package josx.rcxcomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * A Proxy for sending HTTP requests to the RCX.
 * 
 * Listens on the httpPort specified by -httpPort <httpPort>(default 80). If the RCX is
 * currently servicing a request, it returns the massge "RCX is Busy".
 * 
 * The constructor does not return - it runs forever.
 * 
 * @author Lawrie Griffiths
 * 
 */
public class HttpProxy
{
   private boolean busy = false;

   public HttpProxy (int httpPort,String towerPort) throws IOException
   {

      ServerSocket serverSock = new ServerSocket(httpPort);

      while (true)
      {
         System.out.println("Waiting for request on httpPort " + httpPort);
         Socket sock = serverSock.accept();
         System.out.println("Received request");
         if (busy)
         {
            System.out.println("RCX is busy");
            BufferedReader in = new BufferedReader(new InputStreamReader(sock
               .getInputStream()));
            in.readLine();
            PrintWriter pw = new PrintWriter(sock.getOutputStream());
            pw.println("HTTP/1.0 200");
            pw.println("Content-Type: text/html");
            pw.println();
            pw.println("<HTML><BODY>RCX is Busy</BODY></HTML>");
            pw.close();
            // sock.close();
         }
         else
         {
            setBusy(true);
            RCXThread rcxThread = new RCXThread(this,sock,towerPort);
            rcxThread.start();
         }
      }
   }

   /**
    * Called by RCXThread when the RCX has processed a request
    */
   synchronized void setBusy (boolean busy)
   {
      this.busy = busy;
   }

   /**
    * 
    * @param args -httpPort <http port> -towerPort <tower port>
    * @throws IOException
    */
   public static void main (String[] args) throws IOException
   {
      int httpPort = 80;
      String towerPort = "USB";

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-httpPort"))
         {
            if (i < args.length - 1)
               httpPort = Integer.parseInt(args[i + 1]);
            i++;
         }
         if (args[i].equals("-towerPort"))
         {
            if (i < args.length - 1)
               towerPort = args[i + 1];
            i++;
         }
      }

      // Runs forever
      new HttpProxy(httpPort,towerPort);
   }

   /**
    * Innner class to deal with a single request
    */
   class RCXThread extends Thread
   {
      private Socket sock;
      private HttpProxy proxy;
      private String towerPort;

      RCXThread (HttpProxy proxy, Socket sock,String towerPort)
      {
         this.sock = sock;
         this.proxy = proxy;
      }

      public void run ()
      {
         try
         {

            // Open the RCXPort and get the httpPort and socket streams

            RCXPort httpPort = new RCXPort(towerPort);
            InputStream is = httpPort.getInputStream();
            OutputStream os = httpPort.getOutputStream();

            InputStream in = sock.getInputStream();
            OutputStream out = sock.getOutputStream();

            byte b = 0, lastB = 0, lastB2 = 0, lastB3 = 0;
            int inb;

            // Write the Get

            do
            {
               lastB = b;
               b = (byte) in.read();
               System.out.println("Input Stream: " + (char) b);
               os.write(b);
            }
            while (!(b == '\n' && lastB == '\r'));

            os.write((byte) '\r');
            os.write((byte) '\n');

            // Skip the headers

            System.out.println("Skipping headers");

            do
            {
               lastB3 = lastB2;
               lastB2 = lastB;
               lastB = b;
               b = (byte) in.read();
            }
            while (!(b == '\n' && lastB == '\r' && lastB2 == '\n' && lastB3 == '\r'));

            // Read the reply

            do
            {
               inb = is.read();
               if (inb == 0xff)
               {
                  out.flush();
                  sock.close();
                  System.out.println("Socket closed");
               }
               else
               {
                  System.out.println("Output Stream: " + (char) inb);
                  out.write(inb);
               }
            }
            while (inb != 0xff);

            httpPort.close();
            proxy.setBusy(false);
         }
         catch (IOException ioe)
         {
        	 ioe.printStackTrace();
         }
      }
   }
}
