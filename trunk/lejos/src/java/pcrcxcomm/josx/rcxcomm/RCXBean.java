package josx.rcxcomm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles communication with a LEGO RCX Based on original code by the LEGO3
 * Team at DTU-IAU
 * 
 * @author Brian Bagnall
 */
public class RCXBean implements java.io.Serializable
{
   private String comPort = null;
   private RCXPort port;
   private InputStream in;
   private OutputStream out;
   private boolean open = false;
   private Object owner = null;

   /** Creates new RCXBean */
   public RCXBean ()
   {}

   private void openComPort () throws IOException
   {
      // Garbage collect to remove any unused port-users
      System.gc();
      if (comPort == null)
         throw new IOException("COM port not defined");
      port = new RCXPort(comPort);
      in = port.getInputStream();
      out = port.getOutputStream();
      port.setTimeOut(20000);
      open = true;
   }

   /**
    * Getter for the property "ComPort".
    * 
    * @return The name of the comPort used by the bean.
    */
   public String getComPort ()
   {
      return comPort;
   }

   /**
    * Change or set which comPort should be used for communication with the RCX
    * 
    * @param value A identifier for the comPort e.g. <CODE>COM1</CODE> or
    *           <CODE>/dev/ttyS0</CODE>
    * @throws IOException If the initialysation of the comPort goes wrong.
    */
   public void setComPort (String value) throws IOException
   {
      if (!value.equals(comPort))
      {
         close();
         comPort = value;
         openComPort();
      }
      else
         openComPort();
   }

   /**
    * Get n bytes from the RCX
    * 
    * @param n Number of bytes
    * @throws IOException If anything goes wrong
    * @return The bytes sent from the RCX
    * @deprecated Use <code>receive</code> instead.
    */
   /*
    * public byte[] getMessage(int n) throws IOException { byte[] b = new
    * byte[n]; port.setListen(true); try { for (int i=0; i <n; i++) b[i] =
    * (byte)in.read(); } finally { port.setListen(false); } return b; }
    */
   /**
    * Send a byte array to the RCX.
    * 
    * @param b The byte array to send
    * @throws IOException If anything goes wrong
    * @deprecated Use <code>send</code> instead.
    */
   /*
    * public void setMessage(byte[] b) throws IOException { if (!open)
    * openComPort(); out.write (b); out.flush (); }
    */
   /**
    * Send an integer to the RCX. The integer is sent as four bytes.
    * 
    * @param v The integer to be sent.
    * @throws IOException If the integer can not be sent.
    */
   public void sendInt (int v) throws IOException
   {
      if (!open)
         openComPort();

      //for(byte i=24;i>=0;i-=8)
      //   out.write((int)(v >>> i) & 0xFF);
      out.write((v >>> 24) & 0xFF);
      out.write((v >>> 16) & 0xFF);
      out.write((v >>> 8) & 0xFF);
      out.write((v >>> 0) & 0xFF);
      out.flush();
   }

   /**
    * Send a byte to the RCX.
    * 
    * @param b The byte to send.
    * @throws IOException If the byte can not be sent.
    */
   public void send (byte b) throws IOException
   {
      if (!open)
         openComPort();
      out.write(b);
      out.flush();
   }

   /**
    * Send a byte array to the RCX
    * 
    * @param b The byte array to send.
    * @throws IOException If the byte array can not be sent.
    */
   public void send (byte[] b) throws IOException
   {
      if (!open)
         openComPort();
      out.write(b);
      out.flush();
   }

   /**
    * Receive a byte from the RCX.
    * 
    * @throws IOException If the read times out, or something else goes wrong.
    * @return A byte sent from the RCX.
    */
   public byte receive () throws IOException
   {
      if (!open)
         openComPort();
      try
      {
         port.setListen(true);
         return (byte) in.read();
      }
      finally
      {
         port.setListen(false);
      }
   }

   /**
    * Receive a byte from the RCX.
    * 
    * @throws IOException If the read times out, or something else goes wrong.
    * @return A byte sent from the RCX.
    */
   // !! The try-finally block is unnecessary if I do this in PCDataPort !!
   public int receiveInt () throws IOException
   {
      if (!open)
         openComPort();
      try
      {
         port.setListen(true);
         int ch1 = in.read();
         int ch2 = in.read();
         int ch3 = in.read();
         int ch4 = in.read();
         if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new IOException("The bytes were negative");
         return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
      }
      finally
      {
         port.setListen(false);
      }
   }

   /**
    * Receive <code>n<code> bytes from the RCX.
    * @param n The number of bytes to receive.
    * @throws IOException If the read times out.
    * @return A byte array with bytes from the RCX.
    */
   public byte[] receive (int n) throws IOException
   {
      if (!open)
         openComPort();
      port.setListen(true);
      try
      {
         byte[] b = new byte[n];
         for (int i = 0; i < n; i++)
            b[i] = (byte) in.read();
         return b;
      }
      finally
      {
         port.setListen(false);
      }
   }

   /**
    * Create a lock of this the RCX bean. The RCX bean can not be locked by an
    * other object, before the <code>free()</code> has bin run. <br>
    * <B>Caution!! <b>The lock does not prevent other thread from using the RCX
    * bean!
    * 
    * @param o The lock is bound to this object. It should therefore be unique
    *           for the thread.
    * @throws IOException If an other object has allready locked the RCX bean.
    */
   public synchronized void lock (Object o) throws IOException
   {
      if (owner == null)
         owner = o;
      else
         throw new IOException("The RCX is in use");
   }

   /**
    * Make the RCX bean available for other threads.
    * 
    * @param o The object which has the lock.
    */
   public synchronized void free (Object o)
   {
      if (o == owner)
      {
         owner = null;
         close();
      }
   }

   /**
    * Close this RCX bean.
    */
   public void close ()
   {
      open = false;
      if (port != null)
         port.close();
   }

   public void finalize ()
   {
      close();
   }

   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 3544391413744809527L;
}

