package josx.platform.rcx; 

/**
 * Listener of incomming serial data.
 * @see josx.platform.rcx.Serial#addSerialListener
 */

public interface SerialListener 
{   
  /**
   * Called when a packet is received through the IR.
   * @param packet The packet data received.
   * @param length The length of the packet.
   */
  public void packetAvailable (byte[] packet, int length); 
}

