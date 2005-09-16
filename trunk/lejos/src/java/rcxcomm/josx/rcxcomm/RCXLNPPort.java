package josx.rcxcomm;

import java.io.IOException;

/** RCXLNPPort provides an interface similar to java.net.Socket
 * Adapted from original code created by the LEGO3 Team at DTU-IAU
 * This version of RCXPort uses the Legos Network protocol (LNP).
 * This allow communication with LegOS programs or PC LegOS applications.
 * This version of RCXPort uses the Integrity Layer of LNP. This ensures 
 * packets are not corrupted, but does not ensure that they get through. 
 * Packets can get lost. This version does not support addressing.  
 * A two layer protocol stack is used consisting of LNPIntegrityHandler
 * and LNPHandler.
 * @author Brian Bagnall
 * @author Lawrie Griffiths
 */
public class RCXLNPPort extends RCXAbstractPort {
  public RCXLNPPort() throws IOException {
    super((PacketHandler) new LNPIntegrityHandler(
                       (PacketHandler) new LNPHandler(), (byte) 0xf0));
  }
}
