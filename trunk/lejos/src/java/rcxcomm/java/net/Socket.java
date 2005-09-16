package java.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import josx.rcxcomm.RCXPort;

/** An RCX version of Socket that connects to TCP/IP ports via a 
 * proxy called SocketProxy running on the PC. It uses the 
 * josx.rcxcomm package.
 * @author Lawrie Griffiths
 */
public class Socket {
  private RCXPort rcxport;
  private InputStream is;
  private OutputStream os;

  /**
   *  Connects to a Socket via the proxy on the PC.
   * @param host the host to connect to
   * @param port the port to connect to
   */
  public Socket(String host, int port) throws IOException {
    
    // Open the RCX Port

    rcxport = new RCXPort();
    is = rcxport.getInputStream();
    os = rcxport.getOutputStream();
    DataOutputStream dos = new DataOutputStream(os);

    // Send an end of stream marker

    dos.writeByte((byte) 0xff);

    // Send the host name
      
    char [] hostChars = host.toCharArray();

    dos.writeByte(hostChars.length);

    for(int i=0;i<hostChars.length;i++) 
      dos.writeByte((byte) hostChars[i]);

    // Send the port number
     
    dos.writeInt(port);

    // Check the reply

    if (is.read() != 0) throw new IOException();
  }

  /** Returns an input stream for this Socket.
   * @return an input stream for reading bytes from this Socket.
   */
  public InputStream getInputStream() {
    return is;
  }
 
  /** Returns an output stream for this Socket.
   * @return an output stream for writing bytes to this Socket.
   */
  public OutputStream getOutputStream() {
    return os;
  }
}
