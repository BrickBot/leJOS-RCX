package josx.rcxcomm;

import josx.platform.rcx.*;

/**
 * Low-level comms (LLC). This class provide native methods to read and write 
 * single bytes to and from the IR unit by low-level driving of the RCX H8300 
 * UART. It keeps its footprint down by making as much use of ROM routines as possible.
 * The init() method must be called to take over driving the IR unit from the ROM
 * routines. Once this is done, the program cannot use the Serial class. 
 **/
public class LLC {
  /** 
   * Initialize LLC
   **/
  public static native void init();

  /**
   * read a single byte, if available
   * @return the byte read, or -1 if no byte is available
   **/
  public static native int read();

  private static native void write(byte b);

  /**
   * write a single byte and wait for the completion of the transmission
   * @param b the byte to send
   * @result true if byte sent successfully, else false
   **/
  public static boolean send(byte b) {
    LLC.write(b);
    try {Thread.sleep(10);} catch (InterruptedException ie) {}
    return true;
  }

  /**
   * Send a number of bytes
   * @param buf the array of bytes to send
   * @param len the number of bytes to send
   * *result true if the send is succesful, else false
   **/
  public static boolean sendBytes(byte [] buf, int len) {
    for(int i=0;i<len;i++) {
      if (!LLC.send(buf[i])) return false;
    }
    return true;
  }

  /**
   * wait for a byte to become available
   * @result the byte received
   **/
  public static int receive() {
    int r;
    for(int i=0;i<10;i++) {
      r = LLC.read();
      if (r >= 0) return r; 
      Thread.yield();
    }
    return -1;
  }

  /**
   * receive an aray of bytes
   * @param buf the byte array to receive into
   * @param len the number of bytes to receive
   * @param offset the offset n the array to write the first byte
   **/
  public static int receiveBytes(byte [] buf, int len, int offset) {
    for(int i=0; i<len; i++) buf[offset+i] = (byte) LLC.receive(); 
    return len;
  }

  /**
   * Sets long range transmision.
   */
  public static void setRangeLong()
  {
    ROM.call ((short) 0x3250, (short) 0x1770);	  
  }

  /**
   * Sets short range transmision.
   */
  public static void setRangeShort()
  {
    ROM.call ((short) 0x3266, (short) 0x1770);	  
  }
}

