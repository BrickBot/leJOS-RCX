package josx.rcxcomm;

import java.io.*;

/** RCXPort provides an interface similar to java.net.Socket
 * Adapted from original code created by the LEGO3 Team at DTU-IAU
 * Uses Reliable low-level comms for communication.
 * This is a two-layer comms stack consisting of LLCReliableHandler
 * and LLCHandler. It ensures that all packets get through.
 * Communication will stop when the IR tower is not in view or in range,
 * and will resume when it comes back into view.
 * RCXPort does not support addressing - it broadcasts messages to all devices.
 * @author Brian Bagnall
 * @author Lawrie Griffiths
 */
public class RCXPort extends RCXAbstractPort {
  public RCXPort() throws IOException {
    super((PacketHandler) new LLCReliableHandler(
                       (PacketHandler) new LLCHandler()));
    if (packetHandler.getError() != 0) throw new IOException("Tower open failed");
  }

  public RCXPort(String port) throws IOException {
    super(port,(PacketHandler) new LLCReliableHandler(
                       (PacketHandler) new LLCHandler()));
    if (packetHandler.getError() != 0) throw new IOException("Tower open failed");
  }
}
