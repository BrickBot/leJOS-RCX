package josx.rcxcomm;

import java.io.File;
import java.net.URL;

/**
 * Low-level interface to the Lego IR Tower Used by RCXPort to send and receive
 * messages to and from the RCX. Can be used to send any packet or any sequence
 * of bytes to the RCX. The tower is opened with a call to open() or open(port)
 * and closed by a call to close(). <code>send</code> can be used to send a
 * packet, and receive to receive one. <code>write</code> can read a sequence
 * of bytes, and read can read them.
 */
public class Tower
{
   //
   // java implementation
   //

   private String _tty;
   private boolean _isOpen;

   /**
    * Create the tower class.
    */
   public Tower ()
   {
      this("", false);
   }

   /**
    * Create the tower class.
    */
   public Tower (String tty, boolean fastMode)
   {
      assert tty != null: "Precondition: tty != null";

      _tty = tty;
      _isOpen = false;
      // TODO include this if native implementation compiles
      // setFast(fastMode? 1 : 0);
   }

   /**
    * Open the tower.
    */
   public void openTower () throws TowerException
   {
      int status = open(_tty);
      if (status != 0)
      {
         throw new TowerException(status);
      }
      _isOpen = true;
   }

   /**
    * Close tower.
    */
   public void closeTower () throws TowerException
   {
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
   public int getError ()
   {
      return err;
   }

   /**
    * Getter for USB Flag
    * 
    * @return USB Flag as an integer
    */
   public boolean isUSB ()
   {
      return usbFlag == 1;
   }

   /**
    * Check if RCX is alive.
    */
   public boolean isRCXAlive ()
   {
      return isAlive() == 1;
   }

   /**
    * Write low-level bytes to the tower, e.g 0xff550010ef10ef for ping.
    * 
    * @param data bytes to send
    */
   public void writeBytes (byte[] data) throws TowerException
   {
      assert data != null: "Precondition: data != null";

      int status = write(data, data.length);
      if (status < 0)
      {
         throw new TowerException(status);
      }
   }

   /**
    * Send a packet to the RCX, e.g 0x10 for ping.
    * 
    * @param data packet to send
    */
   public void sendPacket (byte[] data) throws TowerException
   {
      assert data != null: "Precondition: data != null";

      int status = send(data, data.length);
      if (status < 0)
      {
         throw new TowerException(status);
      }
   }

   /**
    * Low-level read.
    * 
    * @param data buffer to receive bytes
    * @return number of bytes read
    */
   public int readBytes (byte[] data) throws TowerException
   {
      assert data != null: "Precondition: data != null";

      int result = read(data);
      if (result < 0)
      {
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
   public int receivePacket (byte[] data) throws TowerException
   {
      int result = receive(data);
      if (result < 0)
      {
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
      throws TowerException
   {
      TowerException towerException = null;
      int numRead = -1;
      for (; retries > 0; retries--)
      {
         towerException = null;
         try
         {
            sendPacket(data);
            numRead = receivePacket(response);
            break;
         }
         catch (TowerException e)
         {
            towerException = e;
            // wait 100ms before trying again
            try
            {
               Thread.sleep(100);
            }
            catch (InterruptedException e1)
            {
               // ignore
            }
         }
      }

      if (towerException != null)
      {
         throw towerException;
      }

      assert numRead >= 0: "Postcondition: numRead >= 0";
      return numRead;
   }

   //
   // native code
   //

   /**
    * load native lib.
    */
   static
   {
      try
      {
         // try to search in the directory in which the jar resides
         String filename = System.mapLibraryName("jirtrcx");
         URL url = Tower.class.getResource("Tower.class");
         String jarFilename = url.getFile();
         // cut "file:" and jar part beginning with "!"
         File jarFile = new File(jarFilename.substring(5, jarFilename
            .indexOf('!')));
         File file = new File(jarFile.getParentFile(), filename);
         String path = file.getAbsolutePath();
         // System.err.println("Loading native lib " + path);
         System.load(path);
      }
      catch (Throwable e)
      {
         // System.err.println("Unable to load native lib jirtrcx: " +
         // e.getMessage());

         try
         {
            // try again the default way
            // System.err.println("Loading native lib jirtrcx");
            System.loadLibrary("jirtrcx");
         }
         catch (Throwable e1)
         {
            // System.err.println("Unable to load native lib jirtrcx: " +
            // e1.getMessage());
         }
      }
   }

   // native fields
   private int err;
   private long fh;
   private int usbFlag;

   /**
    * Open the tower
    * 
    * @param port port to use, e.g. usb or COM1
    */
   protected final native int open (String p);

   /**
    * Close the tower
    * 
    * @return error number or zero for success
    */
   protected final native int close ();

   /**
    * Set fast mode
    * 
    * @param fast - 0 = slow mode, 1 = fast mode
    */
   protected final native int setFast (int fast);

   /**
    * Write low-level bytes to the tower, e.g 0xff550010ef10ef for ping.
    * 
    * @param b bytes to send
    * @param n number of bytes
    * @return error number
    */
   protected final native int write (byte b[], int n);

   /**
    * Send a packet to the RCX, e.g 0x10 for ping.
    * 
    * @param b packet to send
    * @param n number of bytes
    * @return error number
    */
   protected final native int send (byte b[], int n);

   /**
    * Low-level read.
    * 
    * @param b buffer to receive bytes
    * @return number of bytes read
    */
   protected final native int read (byte b[]);

   /**
    * Receive a packet.
    * 
    * @param b buffer to receive packet
    * @return number of bytes read
    */
   protected final native int receive (byte b[]);

   /**
    * Dump hex to standard out.
    * 
    * TODO remove?
    * 
    * @param prefix identifies the dump
    * @param b bytes to dump
    * @param n numberof bytes
    */
   protected final native void hexdump (String prefix, byte b[], int n);

   /**
    * Check if RCX is alive
    * 
    * @return 1 if alive, 0 if not
    */
   protected final native int isAlive ();
}