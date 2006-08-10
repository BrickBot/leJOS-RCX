package josx.rcxcomm;

/**
 * A Packet handler that implements the outer LNP packet format. It uses the
 * Tower call to send and receive LNP packets.
 */
public class LNPHandler extends PacketHandler
{
   private byte op;
   private Tower tower;
   private boolean gotPacket = false;
   private boolean debug = false;
   private byte[] inPacket = new byte[259];
   private int inPacketLength;
   private int usbFlag;
   private boolean listen = false;
   private byte[] trash = new byte[1];
   private byte[] keepAlive =
   {
      (byte) 0xff
   };
   private long sendTime;
   private boolean isAddressing;

   /**
    * Creates an LNP packet handler and opens the tower
    */
   public LNPHandler (String port)
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
    * Test if last received packet is addressing (or integrity)
    * 
    * @return true if an addressing packet, false if an integrity packet
    */
   public boolean isAddressing ()
   {
      return isAddressing;
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
      synchronized (this)
      {
         if (debug)
         {
            String s = "Sending packet";
            for (int i = 0; i < len; i++)
               s += " " + packet[i];
            System.out.println(s);
         }
         sendTime = System.currentTimeMillis();

         boolean r = (tower.write(packet, len) == len);

         // Read echo for Serial tower
         byte[] echo = new byte[len];
         if (usbFlag == 0)
            tower.read(echo, Tower.DEFAULT_READ_TIMEOUT);

         return r;
      }
   }

   /**
    * Receive a packet.
    * 
    * @param buffer the buffer to receive the packet into
    * @return the number of bytes received
    */
   public int receivePacket (byte[] buffer)
   {
      if (debug)
      {
         String s = "Receiving Packet";
         for (int i = 0; i < inPacketLength; i++)
            s += " " + inPacket[i];
         System.out.println(s);
      }
      gotPacket = false;
      for (int i = 0; i < inPacketLength; i++)
         buffer[i] = inPacket[i];
      return inPacketLength;
   }

   /**
    * Search for the next paket or ack and read it into the relevent buffer an
    * set the flag to say we've got it. Implements the keep-alive sends.
    */
   private void getOp ()
   {
      while (true)
      {
         byte[] b = new byte[1];
         int r = tower.read(b, Tower.DEFAULT_READ_TIMEOUT);
         if (r <= 0)
         {
            // If its a serial tower and we are in listen mode,
            // send a keep alive byte every 3 seconds

            if (usbFlag == 0 && listen)
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
            return;
         }
         op = b[0];
         // if (debug) System.out.println("Got op = " + b[0]);
         if (op == (byte) 0xf0 || op == (byte) 0xf1)
         {
            // if (debug) System.out.println("Got packet");
            r = tower.read(b, Tower.DEFAULT_READ_TIMEOUT);

            // if length byte is not available, discard the packet
            if (r <= 0)
               return;
            gotPacket = true;
            isAddressing = (op == (byte) 0xf1);
            inPacket[0] = op;
            int extra = (b[0] & 0xff) + 1;
            inPacket[1] = b[0];
            byte[] rest = new byte[extra];
            r = tower.read(rest, Tower.DEFAULT_READ_TIMEOUT);
            for (int i = 0; i < r; i++)
               inPacket[i + 2] = rest[i];
            inPacketLength = extra + 2;
            return;
         }
      }
   }

   /**
    * Check if a packet is available
    * 
    * @return true if a Packet is available, else false
    */
   public boolean isPacketAvailable ()
   {
      // if (debug) System.out.println("IsPacketAvailable");
      synchronized (this)
      {
         if (gotPacket)
            return true;
         getOp();
         return gotPacket;
      }
   }

   /**
    * Close the Tower.
    */
   public void close ()
   {
      tower.close();
   }
}

