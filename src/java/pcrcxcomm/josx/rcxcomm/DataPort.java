package josx.rcxcomm;

import java.io.*;

/** An abstract class which provides an interface similar to java.net.Socket
 * Adapted from original code created by the LEGO3 Team at DTU-IAU
 * @author Brian Bagnall
 */
public abstract class DataPort extends java.lang.Object {

   private boolean portOpen = true;
   private Listener listener;
   private int timeOut = DEFAULT_TIMEOUT;
   private static int i, retry, sendTime, now;

   protected RCXInputStream rcxin;
   protected RCXOutputStream rcxout;
   protected Object monitor;

   protected DataPort() {
      monitor = this;
      rcxin = new RCXInputStream(this);
      rcxout = new RCXOutputStream(this);
      listener = new Listener();
      listener.setDaemon(true);
      listener.start();
   }

   /**
    *  The default time-out for the DataPort.
    */
   public static final int DEFAULT_TIMEOUT = 0;

   /** Returns an input stream for this DataPort.
    * @return an input stream for reading bytes from this DataPort.
    */
   public InputStream getInputStream() {
      return (InputStream)rcxin;
   }

   /** Returns an output stream for this DataPort.
    * @return an output stream for writing bytes to this DataPort.
    */
   public OutputStream getOutputStream() {
      return (OutputStream)rcxout;
   }

   /** Closes this DataPort.
    */
   public void close() {
      portOpen = false;
   }

   /** Send a packet using this DataPort.
    * This method is used by the RCXOutputStream to send data.
    * @param b a byte array with data.
    * @param n the number of bytes to send.
    * @throws IOException if the packet could not be sent.
    * @see RCXOutputStream
    */
   protected void sendPacket(byte[] b, int n) throws IOException {
      synchronized (monitor) {
         for (i=0; i<n; i++) {
            retry = 0;
            do {
               sendByte(b[i]);
               sendTime = (int)System.currentTimeMillis();
               do {
                  Thread.yield();
                  now = (int)System.currentTimeMillis();
               } while (!dataAvailable() && now < sendTime+500);
               if (dataAvailable()) {
                  receiveByte();
                  break;
               }
               retry++;
            } while (retry<3);
            if (retry > 2)
               throw new IOException("Failed to Receive Reply");
         }
      }
   }

   /** Getter for property timeOut.
    * @return Value of property timeOut.
    */
   public int getTimeOut() {
      return timeOut;
   }

   /** Setter for property timeOut.
    * @param timeOut New value of property timeOut.
    */
   public void setTimeOut(int timeOut) {
      this.timeOut = timeOut;
   }

   protected abstract void sendByte(byte b) throws IOException;

   protected abstract byte receiveByte() throws IOException;

   protected abstract boolean dataAvailable();

   // !! This thread should be a daemon thread!!
   private class Listener extends Thread {
      byte in;
      public void run() {

         while (portOpen) {
            synchronized (monitor) {
               if (dataAvailable())
                  try {
                     in = receiveByte();
                     sendByte((byte)~in); // Does this verify byte correct?
                     rcxin.add(in);
                  } catch (IOException ioE) { }
            }
            try {
               Thread.sleep(10);
            } catch (InterruptedException iE) { }
         }
      }
   }

   /**
    * Hidden inner class extending InputStream. Only classes
    * in the josx.platform.rcx.comm package have access to this.
    */
   class RCXInputStream extends java.io.InputStream {

   /** The default buffer size for the InputStream is 128 bytes
    */
      public static final int DEFAULT_BUFFER_SIZE = 128;

      private int bufferSize = DEFAULT_BUFFER_SIZE;
      private byte[] buffer;
      private int current = 0, last = 0;
      private int time1, time2, timeOut;
      private DataPort dataPort;

      /** Creates new RCXInputStream
      * @param port The DataPort which should deliver data for to this InputStream
      */
      public RCXInputStream(DataPort port) {
         super();
         dataPort = port;
         buffer = new byte[bufferSize];
      }

      /** Checks if there is any data avaliable on the InputStream
      * @throws IOException Is never thrown
      * @return The number of bytes avaliable on the InputStream
      */
      public int available() throws IOException {
         if (last < current)
            return bufferSize-(current-last);
         else
            return last-current;
      }

      /** Read a single byte from the InputStream. Returns value as
      * an int value between 0 and 255.
      * @throws IOException Is thrown when the read is timed out
      * @return A data byte from the stream
      */
      public synchronized int read() throws java.io.IOException {
         time1 = (int)System.currentTimeMillis();
         timeOut = dataPort.getTimeOut();
         while (available() == 0) {
            if (timeOut != 0) {
               time2 = (int)System.currentTimeMillis();
               if (time2-time1 > dataPort.getTimeOut())
                  throw new IOException("The read timed out");
            }
            try {
               Thread.sleep(10);
            } catch (InterruptedException iE) { }
         }

         synchronized (buffer) {
            int b = buffer[current++];
            if (current == bufferSize)
               current = 0;

            if(b < 0) b = b + 256;
            return b;
         }
      }

      /** Add a data byte to the stream
      * This method should only be called by the DataPort that
      * created the RCXInputStream
      * @param b The data byte
      */
      protected void add(byte b) {
         synchronized (buffer) {
            buffer[last++] = b;
            if (last == bufferSize)
               last = 0;
         }
      }

      /** Set the buffer size of this RCXInputStream.
      * @param n The number of bytes.
      */
      protected void setBufferSize(int n) {
         this.bufferSize = n;
         if (n > DEFAULT_BUFFER_SIZE)
            buffer = new byte[n];
         current = 0;
         last = 0;
      }
   }

   /** Hidden inner class extending OutputStream. Only classes
    * in the josx.platform.rcx.comm package have access to this.
    */
   class RCXOutputStream extends OutputStream {

      private DataPort dataPort;

      /** Creates new RCXOutputStream
      * @param p The DataPort which should receive data from this OutputStream
      */
      public RCXOutputStream(DataPort p) {
         super();
         this.dataPort = p;
      }

      /** Write a byte to the OutputStream.
      * @param b The byte.
      * @throws IOException If the byte could not be writen to the stream
      */
      public synchronized void write(int b) throws IOException {
         byte [] buffer = new byte[1];
         buffer[0] = (byte) b;
         dataPort.sendPacket(buffer, 1);
      }

      /** Flush the OutputStream
      * @throws IOException If the stream could not be flushed.
      */
      public void flush() throws IOException {
      }

      /** Close the stream.
      * @throws IOException If something goes wrong.
      */
      public void close() throws IOException {
      }
   }
}
