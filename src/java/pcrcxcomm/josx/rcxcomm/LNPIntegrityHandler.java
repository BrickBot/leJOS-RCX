package josx.rcxcomm;

/**
 * Packet handler that implements simple checksums. The checksum is added at the
 * end and is not part of the packet length
 */
public class LNPIntegrityHandler extends PacketHandler
{
   private byte[] inPacket = new byte[259];
   private byte[] outPacket = new byte[259];
   private int inPacketLength = 0;
   private byte op;
   private boolean isAddressing;
   private boolean debug = false;

   /**
    * Create a packet handler for broadcast or addressing integrity packets
    * 
    * @param handler the lower level LNP packet handler
    * @param op the opcode to use for writes: 0xf0 for broadcasts, 0xf1 for
    *           addressing packets
    */
   public LNPIntegrityHandler (PacketHandler handler, byte op)
   {
      super(handler);
      this.op = op;
   }

   /**
    * Test if last packet is an adressing (or integrity) packet
    * 
    * @return true if addressing packet, false if a broadcast
    */
   public boolean isAddressing ()
   {
      return isAddressing;
   }

   /**
    * Set the opcode for the next write
    * 
    * @param op oxf0 for a broadcast, oxf1 for an adressing write
    */
   public void setOp (byte op)
   {
      this.op = op;
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
      int sum = op + len - 1;
      outPacket[0] = op;
      outPacket[1] = (byte) len;
      for (int i = 0; i < len; i++)
      {
         sum += packet[i];
         outPacket[i + 2] = packet[i];
      }
      outPacket[len + 2] = (byte) sum;
      return lowerHandler.sendPacket(outPacket, len + 3);
   }

   /**
    * Receive a packet.
    * 
    * @param buffer the buffer to receive the packet into
    * @return the number of bytes received
    */
   public int receivePacket (byte[] buffer)
   {
      int temp = inPacketLength - 3;
      for (int i = 0; i < temp; i++)
         buffer[i] = inPacket[i + 2];
      inPacketLength = 0;
      return temp;
   }

   /**
    * Check if a packet is available
    * 
    * @return true if a Packet is available, else false
    */
   public boolean isPacketAvailable ()
   {
      if (inPacketLength > 0)
         return true;
      while (lowerHandler.isPacketAvailable())
      {
         int len = lowerHandler.receivePacket(inPacket);
         int sum = -1;
         for (int i = 0; i < len - 1; i++)
            sum += inPacket[i];
         if ((byte) sum == inPacket[len - 1])
         {
            inPacketLength = len;
            isAddressing = ((LNPHandler) lowerHandler).isAddressing();
            return true;
         }
         else
         {
            if (debug)
               System.out.println("Checksum error");
         }
      }
      return false;
   }
}

