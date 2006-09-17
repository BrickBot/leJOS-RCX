package josx.rcxcomm;

/**
 * Low-level interface to the Lego IR Tower Used by RCXPort to send and receive
 * messages to and from the RCX. Can be used to send any packet or any sequence
 * of bytes to the RCX. The tower is opened with a call to open() or open(port)
 * and closed by a call to close(). <code>send</code> can be used to send a
 * packet, and receive to receive one. <code>write</code> can read a sequence
 * of bytes, and read can read them.
 *
 * Note that the methods that have returned error codes in former releases are throwing exceptions now 
 * (in particular the <code>open()</code> methods).
 * Additionally, the <code>Tower()</code> constructor with no String argument for the port has beeen removed, for it lead to multiple errors.
 * The <code>send(byte[],int)</code> method is deprecated now and no longer public.
 */
public class Tower
{
   //
   // java implementation
   //

   public static final int DEFAULT_READ_TIMEOUT = 100;

   private String _tty;
   private boolean _isOpen;

   /**
    * Create the tower class.
    */
   public Tower (String tty) {
      assert tty != null: "Precondition: tty != null";

      _tty = tty;
      _isOpen = false;
   }

   /**
    * Open the tower.
    */
   public void openTower (boolean fastMode) throws TowerException {
      int status = open(_tty, fastMode);
      if (status != 0)
      {
         throw new TowerException(status);
      }
      _isOpen = true;
   }

   /**
    * Close tower.
    */
   public void closeTower () throws TowerException {
      int status = close();
      _isOpen = false;
      if (status != 0)
      {
         throw new TowerException(status);
      }
   }

   /**
    * Get last status code.
    * 
    * @deprecated use exception handling instead of status codes
    */
   public int getError () {
      return _error;
   }

   /**
    * Write low-level bytes to the tower, e.g 0xff550010ef10ef for ping.
    * 
    * @param data bytes to send
    */
   public void writeBytes (byte[] data) throws TowerException {
      assert data != null: "Precondition: data != null";

      int status = write(data, data.length);
      if (status < 0) {
         throw new TowerException(status);
      }
   }

   /**
    * Send a packet to the RCX, e.g 0x10 for ping.
    * 
    * @param data packet to send
    */
   public void sendPacket (byte[] data) throws TowerException {
      assert data != null: "Precondition: data != null";

      int status = send(data, data.length);
      if (status < 0) {
         throw new TowerException(status);
      }
   }

   /**
    * Low-level read.
    * 
    * @param data buffer to receive bytes
    * @return number of bytes read
    */
   public int readBytes (byte[] data) throws TowerException {
      return readBytes(data, DEFAULT_READ_TIMEOUT);
   }

   /**
    * Low-level read.
    * 
    * @param data buffer to receive bytes
    * @param timeout read timeout in ms
    * @return number of bytes read
    */
   public int readBytes (byte[] data, int timeout) throws TowerException {
      assert data != null: "Precondition: data != null";
      assert timeout > 0: "Precondition: timeout > 0";

      int result = read(data, timeout);
      if (result < 0) {
         throw new TowerException(result);
      }

      assert result >= 0: "Postcondition: result >= 0";
      return result;
   }

   /**
    * Receive a packet.
    * 
    * @param data buffer to receive packet
    * @return number of bytes read
    */
   public int receivePacket (byte[] data) throws TowerException {
      return receivePacket(data, DEFAULT_READ_TIMEOUT);
   }

   /**
    * Receive a packet.
    * 
    * @param data buffer to receive packet
    * @param timeout read timeout in ms
    * @return number of bytes read
    */
   public int receivePacket (byte[] data, int timeout) throws TowerException {
      assert data != null: "Precondition: data != null";
      assert timeout > 0: "Precondition: timeout > 0";

      int result = receive(data, timeout);
      if (result < 0) {
         throw new TowerException(result);
      }

      assert result >= 0: "Postcondition: result >= 0";
      return result;
   }

   /**
    * Send a packet and retrieve answer.
    * 
    * @param data bytes to send
    * @param response buffer to receive packet
    * @return number of bytes read
    */
   public int sendPacketReceivePacket (byte[] data, byte[] response, int retries)
      throws TowerException {
      return sendPacketReceivePacket(data, response, retries,
         DEFAULT_READ_TIMEOUT);
   }

   /**
    * Send a packet and retrieve answer.
    * 
    * @param data bytes to send
    * @param response buffer to receive packet
    * @param timeout read timeout in ms
    * @return number of bytes read
    */
   public int sendPacketReceivePacket (byte[] data, byte[] response,
      int retries, int timeout) throws TowerException {
      TowerException towerException = null;
      int numRead = -1;
      for (; retries > 0; retries--) {
         towerException = null;
         try {
            sendPacket(data);
            numRead = receivePacket(response, timeout);
            break;
         } catch (TowerException e) {
            towerException = e;
            // wait 100ms before trying again
            try {
               Thread.sleep(100);
            } catch (InterruptedException e1) {
               throw new TowerException("interrupted");
            }
         }
      }

      if (towerException != null) {
         throw towerException;
      }

      assert numRead >= 0: "Postcondition: numRead >= 0";
      return numRead;
   }

   //
   // native code
   //

   /**
    * Load native lib. 
    */
   static {
      try {
         System.err.println("Loading native libs");
         System.loadLibrary("jirtrcx");
         init();
      } catch (Throwable e1) {
         System.err.println("Unable to load native libraries: " + e1.getMessage());
      }
   }

   //
   // native fields
   //

   /**
    * Handle of tower. This field is only for native usage!
    */
   private long _port;

   /**
    * System error code of last operation. Read only field!
    */
   private int _error;

   /**
    * Open the tower
    * 
    * @param p port to use, e.g. usb or COM1
    * @param fastMode open port for fast mode transmissions?
    */
   protected final native int open (String p, boolean fastMode);

   /**
    * Close the tower
    * 
    * @return error number or zero for success
    */
   protected final native int close ();

   /**
    * Check if RCX is alive
    */
   public final native boolean isRCXAlive ();

   /**
    * Is tower an usb tower?.
    */
   public final native boolean isUSB ();

   /**
    * Write low-level bytes to the tower, e.g 0xff550010ef10ef for ping.
    * 
    * @param b bytes to send
    * @param n number of bytes
    * @return error number
    * @deprecated do not call native methods, use #writeBytes instead
    */
   protected final native int write (byte b[], int n);

   /**
    * Send a packet to the RCX, e.g 0x10 for ping.
    * 
    * @param b packet to send
    * @param n number of bytes
    * @return error number
    * @deprecated do not call native methods, use #sendPacket instead
    */
   protected final native int send (byte b[], int n);

   /**
    * Low-level read.
    * 
    * @param b buffer to receive bytes
    * @param timeout read timeout in ms
    * @return number of bytes read
    * @deprecated do not call native methods, use #readBytes instead
    */
   protected final native int read (byte b[], int timeout);

   /**
    * Receive a packet.
    * 
    * @param b buffer to receive packet
    * @param timeout read timeout in ms
    * @return number of bytes read
    * @deprecated do not call native methods, use #receivePacket instead
    */
   protected final native int receive (byte b[], int timeout);

   /**
    * JNI init.
    */
   private static native void init ();
}
