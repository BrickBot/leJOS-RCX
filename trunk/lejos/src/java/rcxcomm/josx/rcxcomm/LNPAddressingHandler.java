package josx.rcxcomm;

/**
 * Packet handler that implements LNP addressing. 
 */
public class LNPAddressingHandler extends PacketHandler {
  private byte [] inPacket = new byte[259];
  private byte [] outPacket = new byte[259];
  private int inPacketLength = 0;
  private byte source, dest;

  public LNPAddressingHandler(PacketHandler handler) {
    super(handler);
  }

  /**
   * Set the source and destination for this connection
   */
  public void open(byte source, byte destination) {
    this.source = source;
    dest = destination;
  }

  /** Send a packet, adding the source and destination addresses
   * @param packet the bytes to send
   * @param len the number of bytes to send
   * @return true if the send was successful, else false
   */
  public boolean sendPacket(byte [] packet, int len) {
    outPacket[0] = dest;
    outPacket[1] = source;
    for(int i=0; i<len; i++) outPacket[i+2] = packet[i];
    return lowerHandler.sendPacket(outPacket, len+2);
  }

  /** Receive a packet.
   * @param buffer the buffer to rceive the packet into
   * @return the number of bytes received
   */
  public int receivePacket(byte [] buffer) {
    int temp = inPacketLength;
    for(int i=0;i<temp;i++) buffer[i] = inPacket[i+2];
    inPacketLength = 0;
    return temp;
  }

  /**
   * Check if a packet is available
   * @return true if a Packet is available, else false
   */
  public boolean isPacketAvailable() {
    if (inPacketLength > 0) return true;
    while (lowerHandler.isPacketAvailable()) {
      int len = lowerHandler.receivePacket(inPacket);
      if (source != inPacket[0] || dest != inPacket[1]) continue;
      inPacketLength = len-2;
      return true;
    }
    return false;
  }
}


