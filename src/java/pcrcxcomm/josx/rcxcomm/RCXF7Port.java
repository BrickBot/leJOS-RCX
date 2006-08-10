package josx.rcxcomm;

import java.io.IOException;

/**
 * RCXF7Port provides an interface similar to java.net.Socket Adapted from
 * original code created by the LEGO3 Team at DTU-IAU This version of RCXPort
 * uses Serial comms, not low-level comms. Serial comms uses the RCX Rom
 * routines which only support Lego firmware opcodes and protocol. The F7 (set
 * message) opcode is used as it allows comms in both direction. This protocol
 * is the same as that used by RCXPort in lejos 1.0.5, although the
 * implementation is slightly different. This protocol is not very suitable for
 * java streams. Bytes can get lost, and they can be delivered more than once.
 * The protocol supported by RCXPort is far better as in provides guaranteed
 * delivery of all data and is much faster. This version is provided for
 * compatibiltity with the lejos 1.0.5 protocol, and possible interaction with
 * Lego firmware programs (such as nqc programs). A two level protocol stack is
 * used consisting of F7DeliveryHanler (which does Acks, and retries) and
 * F7Handler.
 * 
 * @author Brian Bagnall
 * @author Lawrie Griffiths
 */
public class RCXF7Port extends RCXAbstractPort
{
   public RCXF7Port (String port) throws IOException
   {
      super(port, (PacketHandler) new F7DeliveryHandler(
         (PacketHandler) new F7Handler(port)));
      if (packetHandler.getError() != 0)
         throw new IOException("Tower open failed");
   }
}