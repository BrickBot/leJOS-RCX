package josx.rcxcomm;

/**
 * An interface for processing incoming addressing packets on specific ports.
 **/
public interface AddressingHandler {
  /**
   * Process the adressing packet.
   * @param packet the addressing packet
   * @param len the length of the packet
   * @param source the machine and port address to return replies to
   **/
  void processAddressingPacket (byte [] packet, int len, byte source);
}
 
  
