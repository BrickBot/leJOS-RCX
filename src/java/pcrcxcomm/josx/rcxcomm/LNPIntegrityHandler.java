package josx.rcxcomm;

/**
 * Packet handler that implements simple checksums. 
 * The checksum is added at the end and is not part of the packet length 
 */
public class LNPIntegrityHandler extends PacketHandler {
  private byte [] inPacket = new byte[259];
  private byte [] outPacket = new byte[259];
  private int inPacketLength = 0;
  private byte op;
  private int error = 0;
  private boolean debug = true;

  public LNPIntegrityHandler(PacketHandler handler, byte op) {
    super(handler);
    this.op = op;
  }

  /** Send a packet.
   * @param packet the bytes to send
   * @param len the number of bytes to send
   * @return true if the send was successful, else false
   */
  public boolean sendPacket(byte [] packet, int len) {
    int sum = op + len -1;
    outPacket[0] = op;
    outPacket[1] = (byte) len;
    for(int i=0; i<len; i++) {
      sum += packet[i];
      outPacket[i+2] = packet[i];
    }
    outPacket[len+2] = (byte) sum;
    return lowerHandler.sendPacket(outPacket, len+3);
  }

  /** Receive a packet.
   * @param buffer the buffer to receive the packet into
   * @return the number of bytes received
   */
  public int receivePacket(byte [] buffer) {
    int temp = inPacketLength - 3;
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
      int sum = -1;
      for(int i=0; i<len-1; i++) sum += inPacket[i];
      if ((byte) sum == inPacket[len-1]) {
        inPacketLength = len;
        return true;
      } else {
        if (debug) System.out.println("Checksum error");
        error++;
      }
    }
    return false;
  }
}


