package josx.rcxcomm;

/**
 * An interface for processing incoming broadcasts of LNP integrity packets.
 */
public interface IntegrityHandler
{
   /**
    * Process the broadcast integrity packet.
    * 
    * @param packet the broadcast packet
    * @param len the length of the packet
    */
   void processIntegrityPacket (byte[] packet, int len);
}

