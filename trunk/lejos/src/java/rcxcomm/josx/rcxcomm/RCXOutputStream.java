package josx.rcxcomm;

import java.io.*;
import josx.platform.rcx.*;

/** Cut down version of RCXPort, which only supports writes.
 */
public class RCXOutputStream extends OutputStream {
  private Object monitor;
  private int sendTime;
  private IOException ioe;

  /** Creates new RCXOutputStream
   */
  public RCXOutputStream() {
    super();
    monitor = this;
    ioe = new IOException();
  }

  /** Write a byte to the OutputStream.
   * @param b The byte.
   * @throws IOException if the byte could not be written to the stream
   */
  public synchronized void write(int b) throws IOException {
    sendPacket((byte) b);
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
  private static byte[] buffer = new byte[2];
  private byte receiveByte() throws IOException {
    Serial.readPacket(buffer);
    return buffer[1];
  }  

  /** Send a packet to the RCX.
   * @param b a byte  to send
   * @throws IOException if the packet could not be sent.
   * @see RCXOutputStream
   */
  private void sendPacket(byte b) throws IOException {
    synchronized (monitor) {
      for(int retry=0;retry<3;retry++) {
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
      throw ioe;
    }
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

