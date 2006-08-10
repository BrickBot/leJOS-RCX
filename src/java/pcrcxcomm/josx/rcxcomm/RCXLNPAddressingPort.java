package josx.rcxcomm;

import java.io.IOException;

/**
 * RCXLNPAddressingPort provides an interface similar to java.net.Socket Adapted
 * from original code created by the LEGO3 Team at DTU-IAU This version of
 * RCXPort supports the Lego Network protocol (LNP) addressing layer. This
 * supports point-to-point connections. A single byte (for each) specifies the
 * source and destination. Usually the most significant 4-bites specifies the
 * host, and the least significant 4 bits specifies the port. This version only
 * supports a single active port per host. You can use this port for
 * point-to-point communication between specific RCXs, or between a PC and a
 * specific RCX. The LNP Integrity layer is used to ensures that packets are not
 * corrupted, but they are not guarnteed to get through. A three layer protocol
 * stack is used. consising of LNPAdressingHandler, LNPIntegrityHandler and
 * LNPHandler.
 * 
 * @author Brian Bagnall
 * @author Lawrie Griffiths
 */
public class RCXLNPAddressingPort extends RCXAbstractPort
{
   public RCXLNPAddressingPort (String port, byte source, byte dest)
      throws IOException
   {
      super(port, (PacketHandler) new LNPAddressingHandler(
         (PacketHandler) new LNPIntegrityHandler(
            (PacketHandler) new LNPHandler(port), (byte) 0xf1)));
      if (packetHandler.getError() != 0)
         throw new IOException("Tower open failed");
      packetHandler.open(source, dest);
   }
}