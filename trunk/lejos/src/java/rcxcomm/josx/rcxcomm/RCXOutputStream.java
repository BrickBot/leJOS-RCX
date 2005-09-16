package josx.rcxcomm;

import java.io.IOException;
import java.io.OutputStream;

/** Cut down version of RCXPort, which only supports writes.
 */
public class RCXOutputStream extends OutputStream {

  private IOException ioe;
  private PacketHandler packetHandler;

  /** Creates new RCXOutputStream
   */
  public RCXOutputStream() {
    super();
    packetHandler = (PacketHandler) new LLCReliableHandler(
                       (PacketHandler) new LLCHandler());
    ioe = new IOException();
  }

  private byte[] bytePacket = new byte [1];

  /** Write a byte to the OutputStream.
   * @param b The byte.
   * @throws IOException if the byte could not be written to the stream
   */
  public synchronized void write(int b) throws IOException {
    bytePacket[0] = (byte) b;
    if (!packetHandler.sendPacket(bytePacket,1)) throw ioe;
  }
}

