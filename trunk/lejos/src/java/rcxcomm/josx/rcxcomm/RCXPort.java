package josx.rcxcomm;

import java.io.*;
import josx.platform.rcx.*;

/** RCXPort provides an interface similar to java.net.Socket
 * Adapted from original code created by the LEGO3 Team at DTU-IAU
 * @author Brian Bagnall
 * @author Lawrie Griffiths
 */
public class RCXPort {

   private boolean portOpen = true;
   private Listener listener;
   private int timeOut = DEFAULT_TIMEOUT;
   private static int retry, sendTime;

   protected RCXInputStream rcxin;
   protected RCXOutputStream rcxout;
   protected Object monitor;

   /**
    *  Parameterless constructor for the RCXPort.
    *  Opens the port.
    */
   public RCXPort() throws IOException {
      open();
   }

   /**
    *  The port name is ignored in the RCX version.
    */
   public RCXPort(String port) throws IOException {
      open();
   }   

   /** Creates the input and output streams,
    *  and creates and runs a Listener daemon thread.
    */
   public void open() {
      monitor = this;
      rcxin = new RCXInputStream(this);
      rcxout = new RCXOutputStream(this);
      listener = new Listener();
      listener.setDaemon(true);
      listener.start();
   }

   /**
    *  The default time-out for the RCXPort.
    */
   public static final int DEFAULT_TIMEOUT = 0;

   /** Returns an input stream for this RCXPort.
    * @return an input stream for reading bytes from this RCXPort.
    */
   public InputStream getInputStream() {
      return (InputStream)rcxin;
   }

   /** Returns an output stream for this RCXPort.
    * @return an output stream for writing bytes to this RCXPort.
    */
   public OutputStream getOutputStream() {
      return (OutputStream)rcxout;
   }

   /** Closes this RCXPort.
    */
   public void close() {
      portOpen = false;
   }

   /** Send a packet using this RCXPort.
    * This method is used by the RCXOutputStream to send data.
    * @param b a byte  to send
    * @throws IOException if the packet could not be sent.
    * @see RCXOutputStream
    */
   protected void sendPacket(byte b) throws IOException {
      synchronized (monitor) {
         for(retry=0;retry<3;retry++) {
            sendByte(b);
            sendTime = (int)System.currentTimeMillis();
            do {
               Thread.yield();
            } while (!dataAvailable() && 
                     (int)System.currentTimeMillis() < sendTime+500);
            if (dataAvailable()) {
               receiveByte();
               return;
            }
         }
         throw new IOException();
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

   /** Send a byte using Serial.sendPacket.
    * The 0xf7 (internal message) byte-code is used to send the byte.
    * This is the only Lego-defined packet that can be sent in either direction, 
    * and has no response.
    * @param b a byte  to send
    * @throws IOException is not thrown.
    */
   private byte[] packet = {(byte)0xf7, (byte)0x00};
   protected void sendByte(byte b) throws IOException {
      packet[1] = b;
      Serial.sendPacket(packet, 0, 2);
   }

   /** Check if data is available using RCX Serial Class.
    */
   protected boolean dataAvailable() {
      return Serial.isPacketAvailable();
   }

   /** Receive a byte using Serial.readPacket.
    * @throws IOException is not thrown.
    */
   private static byte[] buffer = new byte[2];
   protected byte receiveByte() throws IOException {
      Serial.readPacket(buffer);
      return buffer[1];
   }  

   /** Listener class runs a thread that reads and buffers bytes.
    *  It send a complement of the byte as an acknowledgement.
    */
   private class Listener extends Thread {
      byte in;
      public void run() {

         while (portOpen) {
            synchronized (monitor) {
               if (dataAvailable())
                  try {
                     in = receiveByte();
                     sendByte((byte)~in); 
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
    * Hidden inner class extending InputStream. 
    */
   class RCXInputStream extends InputStream {

   /** The default buffer size for the InputStream is 32 bytes
    */
      public static final int DEFAULT_BUFFER_SIZE = 32;

      private int bufferSize = DEFAULT_BUFFER_SIZE;
      private byte[] buffer;
      private int current = 0, last = 0;
      private int time1, timeOut;
      private RCXPort dataPort;

      /** Creates new RCXInputStream
      * @param port The RCXPort which should deliver data for to this InputStream
      */
      public RCXInputStream(RCXPort port) {
         super();
         dataPort = port;
         buffer = new byte[bufferSize];
      }

      /** Checks if there is any data avaliable on the InputStream
      * @throws IOException is never thrown
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
      * @throws IOException is thrown when the read is timed out
      * @return A data byte from the stream
      */
      public synchronized int read() throws java.io.IOException {
         time1 = (int)System.currentTimeMillis();
         timeOut = dataPort.getTimeOut();
         while (available() == 0) {
            if (timeOut != 0 && ((int)System.currentTimeMillis()-time1 > timeOut)) {
                  throw new IOException();
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
      * This method should only be called by the RCXPort that
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
   }

   /** Hidden inner class extending OutputStream. 
    */
   class RCXOutputStream extends OutputStream {

      private RCXPort dataPort;

      /** Creates new RCXOutputStream
      * @param p The RCXPort which should receive data from this OutputStream
      */
      public RCXOutputStream(RCXPort p) {
         super();
         this.dataPort = p;
      }

      /** Write a byte to the OutputStream.
      * @param b The byte.
      * @throws IOException if the byte could not be written to the stream
      */
      public synchronized void write(int b) throws IOException {
         dataPort.sendPacket((byte) b);
      }

      /** Flush the OutputStream
      * @throws IOException is never thrown
      */
      public void flush() throws IOException {
      }

      /** Close the stream.
      * @throws IOException is never thrown
      */
      public void close() throws IOException {
      }
   }
}
