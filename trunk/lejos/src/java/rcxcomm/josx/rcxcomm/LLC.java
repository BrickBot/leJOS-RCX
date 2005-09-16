package josx.rcxcomm;

import josx.platform.rcx.ROM;

/**
 * Low-level comms (LLC). This class provide native methods to read and write 
 * bytes to and from the IR unit by low-level driving of the RCX H8300 
 * UART. It keeps its footprint down by making as much use of ROM routines as possible.
 * The init() method must be called to take over driving the IR unit from the ROM
 * routines. Once this is done, the program cannot use the Serial class. 
 **/
public class LLC {
  private static int sendTime;
  private static final int COLLISION_DELAY = 200;

  private LLC() {
  }

  /** 
   * Initialize LLC
   **/
  public static native void init();

  /**
   * read a single byte, if available
   * @return the byte read, or -1 if no byte is available
   **/
  public static native int read();

  private static native void write(byte [] buf, int len);

  /**
   * Indicate whether the last send is still active
   * @return true if still sending, else false
   **/
  public static native boolean isSending();

  /**
   * Return the error status of the last send
   * @return true if still sending, else false
   **/
  public static native boolean isSendError();

  /**
   * Send a number of bytes and wait for completion of transmission
   * @param buf the array of bytes to send
   * @param len the number of bytes to send
   * @return true if the send is successful, else false
   **/
  public static boolean sendBytes(byte [] buf, int len) {
    if (isSending()) return false;
    int currTime = (int)System.currentTimeMillis();
    
    // If there was a collision on the last send, wait a while

    if (isSendError() && currTime - sendTime < COLLISION_DELAY) {
      try {
        Thread.sleep(COLLISION_DELAY - (currTime - sendTime));
      } catch (InterruptedException ie) {} 
    }
    sendTime = (int)System.currentTimeMillis();
    LLC.write(buf, len);
    while (isSending()) Thread.yield();
    return !(isSendError());
  }

  /**
   * wait a little while for a byte to become available
   * @return the byte received, or -1 if no byte available
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

