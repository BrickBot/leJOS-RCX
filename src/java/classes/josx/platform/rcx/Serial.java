package josx.platform.rcx;

/**
 * Low-level API for infra-red (IR) communication between
 * an RCX and the IR tower or between two RCXs.
 * For protocol details, Kekoa Proudfoot's Opcode Reference
 * is highly recommended. See Kekoa's 
 * <a href="http://graphics.stanford.edu/~kekoa/rcx/index.html">RCX Internals</a> page.
 * Kekoa Proudfoot has also written a C based tool
 * (<a href="http://graphics.stanford.edu/~kekoa/rcx/tools.html">Send</a>)
 * which you can use to send packets to the RCX. If you prefer
 * to write everything in Java, you should become familiar with the
 * <a href="http://java.sun.com/products/javacomm/">Java Communications API</a>.
 * Frameworks based on this API have already been developed
 * by Dario Laverde 
 * (see <a href="http://www.escape.com/~dario/java/rcx/">RCXLoader</a>)
 * and Scott Lewis (see <a href="http://www.slewis.com/rcxport/">RCXPort</a>).
 * The Java Communications API is officially supported on Windows and Solaris.
 * <p>
 * Examples that use the leJOS Serial class can be found in:
 * <ul>
 * <li><code>examples/serial</code> --- Receiver for certain opcodes, such as MotorOn.
 * <li><code>examples/serial2rcx</code> --- Communication between two RCXs.  
 * <li><code>examples/remotectl</code> --- Receiver for Lego remote control.
 * </ul>
 * <p>
 * The basic pattern for a Receiver is:
 * <p>
 * <code>
 * <pre>
 *    byte[] packet = new byte[8];
 *    for (;;)
 *    {
 *      if (Serial.isPacketAvailable())
 *      {
 *        Serial.readPacket (packet);
 *        byte opcode = packet[0];
 *        if (opcode == AN_OPCODE)
 *          ...
 *          ...
 *        // Possibly send a response here
 *        packet = ~packet[0];
 *        Serial.sendPacket (packet, 0, PACKET_LENGTH);
 *      }
 *    }
 * </pre>
 * </code>
 */
public class Serial
{
  static final byte[] buffer = new byte[6]; // opcode + at most 5 data 
  private static final byte[] iAuxBuffer = new byte[4];
  private static final int iAuxBufferAddr = Memory.getDataAddress (iAuxBuffer);

  private static SerialListener[] iListeners = null;
  private static int iNumListeners;
  private static SerialListenerCaller singleton;

  private Serial()
  {
  }

  /**
   * Reads a packet received by the RCX, if one is available.
   * The first
   * byte in the buffer is the opcode. Opcode
   * 0x45 (Transfer Data) is received in a special way: If you 
   * had previously called setDataBuffer(), packet data will
   * be copied into the buffer provided. Note the caveats regarding
   * setDataBuffer() use.
   *
   * @return The number of bytes received.
   * @see josx.platform.rcx.Serial#isPacketAvailable
   * @see josx.platform.rcx.Serial#setDataBuffer
   */
  public static int readPacket (byte[] aBuffer)
  {
    synchronized (Memory.MONITOR)
    {
      // Receive packet data
      iAuxBuffer[2] = (byte) 0;
      ROM.call ((short) 0x33b0, (short) Memory.getDataAddress (aBuffer),
                (short) aBuffer.length, (short) (iAuxBufferAddr + 2));
      return (int) iAuxBuffer[2];
    }
  }

  /**
   * Sets the buffer that will be used to save data
   * transferred with opcode 0x45.<p>
   * <b>Note:</b> This method must be used with caution.
   * A pointer to the data buffer is passed to the ROM
   * for asynchronous use.
   * If more data is received than can be stored in the
   * buffer, the VM's memory will be corrupted and
   * it will crash or at least misbehave.
   */
  public static void setDataBuffer (byte[] aData)
  {
    // Set data pointer
    ROM.call ((short) 0x327c, (short) 0x1771, 
              (short) Memory.getDataAddress (aData), (short) 0);
  }

  /**
   * Checks to see if a packet is available.
   * Call this method before calling receivePacket.
   */
  public static boolean isPacketAvailable()
  {
    synchronized (Memory.MONITOR)
    {
      // Check for data
      ROM.call ((short) 0x3426, (short) (iAuxBufferAddr + 3), (short) 0);
      return (iAuxBuffer[3] != 0);
    }
  }

  /**
   * Sends a packet to the IR tower or another RCX.
   * In general, the IR tower will only receive <i>responses</i>
   * to messages it has sent. The call returns immediately.
   * @return false if a packet is already being sent.
   */
  public static boolean sendPacket (byte[] aBuffer, int aOffset, int aLen)
  {
    if (isSending())
        return false;
        
    ROM.call ((short) 0x343e, (short) 0x1775, (short) 0, 
              (short) (Memory.getDataAddress (aBuffer) + aOffset),
              (short) aLen);
                     
    return true;
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
  
  /**
   * Resets serial communications. It can be used
   * to disable buffers set with <code>setDataBuffer</code>.
   */
  public native static void resetSerial();

  /**
   * Return true if a message is being sent.
   */
  public static boolean isSending()
  {
    return Memory.readByte(0xef93) != 0x4f;
  }

  /**
   * Wait until a message has been sent.
   */
  public static void waitTillSent() throws InterruptedException
  {
    while (isSending())
    {
       Thread.sleep(20);
    }
  }

  /**
   * Adds a listener of receive events.  There can be at most
   * 4 listeners.
   */
  public static synchronized void addSerialListener (SerialListener aListener)
  {
    if (iListeners == null)
    {
      iListeners = new SerialListener[4];
      singleton = new SerialListenerCaller();
    }
    iListeners[iNumListeners++] = aListener;
    ListenerThread.get().addSerialToMask(singleton);
  }

  /**
   * private static inner class which allows a ListenerCaller object 
   * to be registered to call the Serial Listeners.
   */
  private static class SerialListenerCaller implements ListenerCaller {

    public synchronized void callListeners()
    {
      int length = Serial.readPacket (Serial.buffer); 
      for( int i = 0; i < Serial.iNumListeners; i++) {
        Serial.iListeners[i].packetAvailable (Serial.buffer, length);
      }
    }
  }
}


