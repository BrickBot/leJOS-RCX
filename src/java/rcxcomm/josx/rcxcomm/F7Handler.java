package josx.rcxcomm;

import josx.platform.rcx.Serial;

/**
 * Packet handler that uses the Lego Firmware F7
 * (set Message) opcodes.
 **/
public class F7Handler extends PacketHandler {
  private byte [] f7pack = {(byte) 0xF7, 0};

  /** Send a packet.
   * @param packet the bytes to send
   * @param len the number of bytes to send
   * @return true if the send was successful, else false
   */
  public boolean sendPacket(byte [] packet, int len) {
    f7pack[1] = packet[0];
    while (Serial.isSending()) Thread.yield();
    return Serial.sendPacket(f7pack, 0, 2);
  }
    
  /** Receive a packet.
   * @param buffer the buffer to receive the packet into
   * @return the number of bytes received
   */
  public int receivePacket(byte [] buffer) {
    return Serial.readPacket(buffer);
  }

  /**
   * Check if a packet is available
   * @return true if a Packet is available, else false
   */
  public boolean isPacketAvailable() {
    return Serial.isPacketAvailable();
  }
}

