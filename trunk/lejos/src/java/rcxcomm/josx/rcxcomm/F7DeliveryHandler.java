package josx.rcxcomm;

/**
 * A packet handler that attempts (but doesn't fully succeed in) reliable
 * delivery for F7 Lego firmware opcode packets.
 * It implements acks, and checks packet validity, but has no sequence
 * number checking. Packets are not guaranteed to get through, and
 * duplicates can occur undetected. See F7Handler for more information.
 **/
public class F7DeliveryHandler extends PacketHandler {
  private byte [] inPacket = new byte[2];

  public F7DeliveryHandler(PacketHandler handler) {
    super(handler);
  }

  /** Send a packet.
   * @param packet the bytes to send
   * @param len the number of bytes to send
   * @return true if the send was successful, else false
   */
  public boolean sendPacket(byte [] packet, int len) {
    synchronized (this) {
      for(int retry=0;retry<3;retry++) {
        int sendTime = (int)System.currentTimeMillis();
        lowerHandler.sendPacket(packet,1);
        do {
          Thread.yield();
        } while ((!lowerHandler.isPacketAvailable()) && 
                 (int)System.currentTimeMillis() < sendTime+500);
        if (lowerHandler.isPacketAvailable()) {
          lowerHandler.receivePacket(inPacket);
          return true;
        }
      }
      return false;
    }
  }

  /** Receive a packet.
   * @param buffer the buffer to receive the packet into
   * @return the number of bytes to send
   */
   public int receivePacket(byte [] buffer) {
    synchronized (this) {
      lowerHandler.receivePacket(inPacket);
      byte b = inPacket[1];
      inPacket[0] = (byte) ~b;
      lowerHandler.sendPacket(inPacket, 1);
      buffer[0] = b;
      return 1;
    }
  }

  public boolean isPacketAvailable() {
    synchronized (this) {
       return lowerHandler.isPacketAvailable();
    }
  } 
}

