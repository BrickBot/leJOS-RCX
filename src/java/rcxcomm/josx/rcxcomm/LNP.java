package josx.rcxcomm;

/**
 * An interface for sending and receiving Lego Network Protocol
 * addressing and integrity layer packets.
 *
 * It uses an integrity packet handler to send and receive integrity and
 * addressing packet.
 *
 * Listeners can be registered to process incoming broadcasts of
 * integrity packets or addressing packets on multiple ports.
 * 
 * Currently assumes that the most significant nibble of the address byte
 * is the machine and the least significant nibble is the port number.
 *
 * The machine addresses is defined by the startListening call, but
 * is also part of the source address sent with addressing packets.
 **/
public class LNP extends Thread {
  private static final int MAX_HANDLERS = 4;
  private static final int MAX_PORTS = 4;
  private static LNPIntegrityHandler packetHandler
    = new LNPIntegrityHandler(new LNPHandler(), (byte) 0xf0); // opcode is ignored
  private static LNP singleton = new LNP();
  private static byte [] outPacket = new byte[64];
  private static byte [] inPacket = new byte[64];
  private static byte [] buff = new byte[64]; 
  private static byte machineAddress;
  private static IntegrityHandler [] integrityHandler 
    = new IntegrityHandler[MAX_HANDLERS];
  private static int numIntegrityHandlers = 0;
  private static AddressingHandler [][] addressingHandler 
    = new AddressingHandler[MAX_PORTS][MAX_HANDLERS];
  private static int [] numAddressingHandlers = new int[MAX_HANDLERS];

  private LNP() {
  }

  /**
   * Sends packet of up to 59 bytes to the destination address from the source
   * address. The source address is used for replying to the packet.
   * @param packet the packet to send
   * @param len the length of the packet
   * @param dest the destination machine and port
   * @param source the source machine and address
   * @return true for successful send, else false, e.g. for collision detection
   **/
  public static boolean addressingWrite(byte [] packet, int len, byte dest, byte source) {
    synchronized(singleton) {
      packetHandler.setOp((byte) 0xf1);
      outPacket[0] = dest;
      outPacket[1] = source;
      for(int i=0; i<len; i++) outPacket[i+2] = packet[i];
      return packetHandler.sendPacket(outPacket, len+2);
    }
  }

  /**
   * Broadcasts an integrity packet of up to 61 bytes
   * @param packet the packet to broadcast
   * @param len the length of the packet
   * @return true if sucessful, else false e.g. for collision detected
   **/
  public static boolean integrityWrite(byte [] packet, int len) {
    synchronized(singleton) {
      packetHandler.setOp((byte) 0xf0);
      return packetHandler.sendPacket(packet, len);
    }
  }

  /**
   * Register an integrity handler to process incoming broadcasts
   * @param handler the integrity handler
   **/
  public static void addIntegrityHandler(IntegrityHandler handler) {
    synchronized(singleton) {
      integrityHandler[numIntegrityHandlers++] = handler;
    }
  }

  /**
   * Register an integrity handler to process incoming addressing
   * packets for a specific port.
   * @param handler the addressing handler
   **/
  public static void addAddressingHandler(AddressingHandler handler, byte port) {
    synchronized(singleton) {
      addressingHandler[port][numAddressingHandlers[port]++] = handler;
    }
  }

  /**
   * Start listening for incoming broadcasts and addressing packets.
   * @param addr the address of this machine. The least significant 4 bits should be zero.
   **/
  public static void startListening(byte addr) {
    machineAddress = addr;
    singleton.setDaemon(true);
    singleton.start();
  }     

  /**
   * Background thread to listen for incoming packets and call the appropriate
   * listeners.
   **/
  public void run() {
    for(;;) {
      while (!packetHandler.isPacketAvailable()) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException ie) {}
      }
      int len = packetHandler.receivePacket(inPacket);
      if (!packetHandler.isAddressing()) { // Broadcast packet
        // call integrity handlers
        for(int i=0;i<numIntegrityHandlers;i++) 
          integrityHandler[i].processIntegrityPacket(inPacket, len);
      } else { // Addressing packet
        if (((byte) (inPacket[0] & 0xf0)) == machineAddress) {
          // call Addressing listeners for port
          for(int i=2;i<len;i++) buff[i-2] = inPacket[i];
          byte port = (byte) (inPacket[0] & 0xf);
          for(int i=0;i<numAddressingHandlers[port];i++) 
            addressingHandler[port][i].processAddressingPacket(buff, len-2, inPacket[1]);   
        }
      } 
    }
  }
}

