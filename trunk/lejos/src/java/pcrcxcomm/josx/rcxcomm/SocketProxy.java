package josx.rcxcomm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A Proxy for the RCX Socket interface. Uses the josx.rcxcomm package to
 * communicate with the RCX. The RCX send the host, port and path.
 * 
 * @author Lawrie Griffiths
 */
public class SocketProxy
{
   private static DataInputStream dis;
   private static DataOutputStream dos;

   private SocketProxy (String port)
   {
      try
      {

         // Open the RCX Port

         RCXPort rcxport = new RCXPort(port);
         InputStream is = rcxport.getInputStream();
         OutputStream os = rcxport.getOutputStream();
         dis = new DataInputStream(is);
         dos = new DataOutputStream(os);

         // Skip the end of stream marker

         is.read();

         // Create a socket

         newSocket();
      }
      catch (IOException ioe)
      {
         System.out.println("IOException: " + ioe.getMessage());
      }
   }

   public static void main (String[] args)
   {
	   if(args.length!=1) {
		   System.err.println("argument tower port (USB, COM1 etc) required!");
		   System.err.println("shutting down");
	   } else
		   new SocketProxy(args[0]);
   }

   /**
    * Open a new socket for a new connection.
    */
   private void newSocket ()
   {
      try
      {
         int len = dis.readByte();

         char[] hostChars = new char[len];

         for (int i = 0; i < len; i++)
            hostChars[i] = (char) dis.readByte();

         String host = new String(hostChars);

         int port = dis.readInt();

         System.out.println("Connecting to host: " + host + ",port: " + port);

         Socket sock = new Socket(host, port);

         dos.writeByte(0);
         dos.flush();

         InputStream in = sock.getInputStream();
         OutputStream out = sock.getOutputStream();
         DataInputStream din = new DataInputStream(in);
         DataOutputStream dout = new DataOutputStream(out);

         new InThread(sock, din, dos);
         new OutThread(sock, dis, dout);
      }
      catch (IOException ioe)
      {
         System.out.println("IOException: " + ioe.getMessage());
      }
   }

   /**
    * private inner class that implements a thread for reading from the socket
    * and writing to thew RCXPort.
    */
   private class InThread extends Thread
   {
      private DataOutputStream dout;
      private DataInputStream din;
      private Socket sock;

      public InThread (Socket sock, DataInputStream dis, DataOutputStream dos)
      {
         super();
         din = dis;
         dout = dos;
         this.sock = sock;
         start();
      }

      public void run ()
      {
         while (true)
         {
            try
            {
               int in = din.readByte();

               // If end of stream, close socket and stop thread

               if (in < 0)
               {
                  System.out.println("Inthread: Closing Socket");
                  sock.close();
                  return;
               }

               System.out.println("Inthread: " + (char) in);
               dout.writeByte(in);
               dout.flush();
            }
            catch (IOException ioe)
            {}
         }
      }
   }

   /**
    * Private inner class that implements a thread for reading from the RCXPort
    * and writing to the socket. When it detects of connection from the RCX, and
    * opens a new socket.
    */
   private class OutThread extends Thread
   {
      private DataOutputStream dout;
      private DataInputStream din;
      private Socket sock;

      public OutThread (Socket sock, DataInputStream dis, DataOutputStream dos)
      {
         super();
         din = dis;
         dout = dos;
         this.sock = sock;
         start();
      }

      public void run ()
      {
         int in = 0;
         while (true)
         {
            try
            {
               in = din.readByte();

               // If we get an end of stream marker, start a new connection

               if (in < 0)
               {
                  System.out.println("Outthread stopped");
                  sock.close();
                  newSocket();
                  return;
               }

               System.out.println("Outthread: " + (char) in);
               dout.writeByte(in);
            }
            catch (IOException ioe)
            {}
         }
      }
   }
}

