package josx.rcxcomm;

/**
 * Packet handler that uses the Lego Firmware F7 (set Message) opcodes.
 */

public class F7Handler extends PacketHandler
{
   private byte[] f7pack =
   {
      (byte) 0xF7, 0
   };
   private byte[] trash = new byte[1];
   private byte[] keepAlive =
   {
      (byte) 0xff
   };
   private Tower tower;
   private byte[] buffer = new byte[9];
   private int usbFlag;
   private int bytesRead = 0;
   private long sendTime;
   private boolean listen = false;
   boolean debug = false;

   F7Handler(String port)
   {
      try
      {
         tower = new Tower(port);
         tower.openTower(false);
         usbFlag = tower.isUSB()? 1 : 0;
      }
      catch (TowerException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Get the last error on the Tower
    * 
    * @return the error number, or zero for success
    */
   public int getError ()
   {
      return tower.getError();
   }

   /**
    * Set or unset the listen flag to keep a PC serial tower alive
    * 
    * @param listen true to set listen mode, else false
    */
   public void setListen (boolean listen)
   {
      this.listen = listen;
   }

   /**
    * Send a packet.
    * 
    * @param packet the bytes to send
    * @param len the number of bytes to send
    * @return true if the send was successful, else false
    */
   public boolean sendPacket (byte[] packet, int len)
   {
      if (debug)
         System.out.println("Sending " + packet[0]);
      f7pack[1] = packet[0];
      sendTime = System.currentTimeMillis();
      return (tower.send(f7pack, 2) == 2);
   }

   /**
    * Receive a packet.
    * 
    * @return packet the number of bytes to send
    */
   public int receivePacket (byte[] packet)
   {
      if (debug)
      {
         String s = "Read";
         for (int i = 0; i < bytesRead; i++)
            s += " " + buffer[i];
         System.out.println(s);
      }

      // ignore leading 0x55
      int start = buffer[0] == (byte) 0x55? 1 : 0;

      if (bytesRead - start < 8)
      {
         if (debug)
            System.out.println("Packet too short");
         bytesRead = 0;
         return 0;
      }

      // check complements
      for (int i = start; i < bytesRead - 1; i += 2)
      {
         if (buffer[i] != ~buffer[i + 1])
         {
            return -1;
         }
      }
      bytesRead = 0;

      // check for format and send message command
      if (buffer[start] != (byte) 0xff || buffer[start + 2] != (byte) 0xf7)
      {
         return -1;
      }
      // extract payload
      packet[0] = buffer[start + 2];
      packet[1] = buffer[start + 4];
      if (debug)
      {
         System.out.println("Read " + packet[1]);
      }
      return 2;
   }

   /**
    * Check if a packet is available
    * 
    * @return true if a Packet is available, else false
    */
   public boolean isPacketAvailable ()
   {
      if (bytesRead > 0)
         return true;

      // Read 9 bytes into the buffer
      bytesRead = tower.read(buffer, Tower.DEFAULT_READ_TIMEOUT);

      // If its a serial tower and we are in listen mode,
      // send a keep alive byte every 3 seconds

      if (usbFlag == 0 && listen && bytesRead == 0)
      {
         long currTime = System.currentTimeMillis();
         if ((currTime - sendTime) >= 3000)
         {
            if (debug)
               System.out.println("Sending keep-alive");
            tower.write(keepAlive, 1);
            tower.read(trash, Tower.DEFAULT_READ_TIMEOUT); // discard
            sendTime = currTime;
         }
      }

      boolean r = (bytesRead > 0);
      return r;
   }

   /**
    * Close the Tower.
    */
   public void close ()
   {
      tower.close();
   }
}

