package josx.rcxcomm;

import java.io.*;
import josx.platform.rcx.*;

/** RCXInputStream provides an interface similar to FileInputStream.
 * It is a cut down version of RCXPort, which provides just read access.
 * Adapted from original code created by the LEGO3 Team at DTU-IAU
 * @author Lawrie Griffiths
 */
public class RCXInputStream extends InputStream {

  /** The default buffer size for the InputStream is 32 bytes
   */
  public static final int DEFAULT_BUFFER_SIZE = 32;
  public static final int DEFAULT_TIMEOUT = 0;

  private int bufferSize = DEFAULT_BUFFER_SIZE;
  private byte[] buffer;
  private int current = 0, last = 0;
  private int time1;
  private IOException ioe;
  private Listener listener;
  private boolean portOpen = true;
  private int timeout = DEFAULT_TIMEOUT;
  
  /** Creates new RCXInputStream
   */
  public RCXInputStream() {
    super();
    buffer = new byte[bufferSize];
    ioe = new IOException();
    listener = new Listener();
    listener.setDaemon(true);
    listener.start();
  }

  /** Send a byte using Serial.sendPacket.
   * The 0xf7 (internal message) byte-code is used to send the byte.
   * This is the only Lego-defined packet that can be sent in either direction, 
   * and has no response.
   * @param b a byte  to send
   * @throws IOException is not thrown.
   */
  private byte[] packet = {(byte)0xf7, (byte)0x00};
  private void sendByte(byte b) throws IOException {
    packet[1] = b;
    Serial.sendPacket(packet, 0, 2);
  }

  /** Check if data is available using RCX Serial Class.
   */
  private boolean dataAvailable() {
    return Serial.isPacketAvailable();
  }

  /** Receive a byte using Serial.readPacket.
   * @throws IOException is not thrown.
   */
  private static byte[] buff = new byte[2];
  private byte receiveByte() throws IOException {
    Serial.readPacket(buff);
    return buff[1];
  }  

  /** Setter for timeout
   * @param timeout the timeout
   */
  public void setTimeOut(int timeout) {
    this.timeout = timeout;
  }

  /** Listener class runs a thread that reads and buffers bytes.
   *  It send a complement of the byte as an acknowledgement.
   */
  private class Listener extends Thread {
    byte in;
    public void run() {
      while (portOpen) {
        if (dataAvailable()) {
          try {
            in = receiveByte();
            sendByte((byte)~in); 
            add(in);
          } catch (IOException ioE) { }
        }
        try {
          Thread.sleep(10);
        } catch (InterruptedException iE) { }
      }
    }
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
    while (available() == 0) {
      if (timeout != 0 && ((int)System.currentTimeMillis()-time1 > timeout)) {
        throw ioe;
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
   * @param b The data byte
   */
  private void add(byte b) {
    synchronized (buffer) {
      buffer[last++] = b;
      if (last == bufferSize)
        last = 0;
    }
  }

  /** Close the stream and stop listening
   */
  public void close() {
    portOpen = false;
  }
}
