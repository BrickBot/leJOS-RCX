package josx.rcxcomm;

/**
 * Packet handler than implement the LLC packet protocol. Deals with packets and
 * acks. Supports independent streams of data in both directions.
 */
public class LLCHandler extends PacketHandler
{
   private byte op;
   private static Tower tower;
   boolean open = false;
   private boolean gotAck = false;
   private boolean gotPacket = false;
   private boolean debug = false;
   private byte[] inPacket = new byte[3];
   private byte[] ackPacket = new byte[2];
   private int inPacketLength;
   private int usbFlag;
   private boolean listen = false;
   private byte[] trash = new byte[1];
   private byte[] keepAlive =
   {
      (byte) 0xff
   };
   private long sendTime;

   public LLCHandler(String port)
   {
      try
      {
         if (tower == null)
            tower = new Tower(port);
         if (!open)
            tower.openTower(false);
         usbFlag = tower.isUSB()? 1 : 0;
         open = true;
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
      synchronized (this)
      {
         if (debug)
         {
            String s = "Sending ";
            if (len == 3)
               s += "packet";
            else
               s += "Ack";
            for (int i = 0; i < len; i++)
               s += " " + packet[i];
            System.out.println(s);
         }
         // System.out.println("Written " + t.write(packet, len));
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
    * Receive a ack.
    * 
    * @param buffer the buffer to receive the ack into
    * @return the number of bytes received
    */
   public int receiveAck (byte[] buffer)
   {
      if (debug)
      {
         String s = "Receiving Ack";
         for (int i = 0; i < 2; i++)
            s += " " + ackPacket[i];
         System.out.println(s);
      }
      gotAck = false;
      for (int i = 0; i < 2; i++)
         buffer[i] = ackPacket[i];
      return 2;
   }

   /**
    * Search for the next packet or ack and read it into the relevant buffer and
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
         if ((op & 0xf7) == 0xf1)
         {
            gotPacket = true;
            inPacket[0] = op;
            int extra = (op & 0x7) + 1; // Add 1 for the checksum
            byte[] rest = new byte[extra];
            r = tower.read(rest, Tower.DEFAULT_READ_TIMEOUT);
            for (int i = 0; i < r; i++)
               inPacket[i + 1] = rest[i];
            inPacketLength = extra + 1;
            return;
         }
         if ((op & 0xf7) == 0xf0)
         {
            gotAck = true;
            ackPacket[0] = op;
            byte[] rest = new byte[1];
            r = tower.read(rest, Tower.DEFAULT_READ_TIMEOUT);
            ackPacket[1] = rest[0];
            return;
         }
         if (debug)
            System.out.println("Discarding " + op);
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
    * Check if an Ack is available
    * 
    * @return true if ack is available, else false
    */
   public boolean isAckAvailable ()
   {
      synchronized (this)
      {
         if (gotAck)
            return true;
         getOp();
         return gotAck;
      }
   }

   /**
    * Close the Tower.
    */
   public void close ()
   {
      tower.close();
      open = false;
   }
}

