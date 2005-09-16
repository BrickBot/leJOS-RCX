package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** An RCX version of URLConnection that uses the RCX version of Socket. 
 * Socket uses a proxy called SocketProxy running on the PC. It connects  
 * to it using the josx.rcxcomm package.
 * This version ignores all http headers.
 * @author Lawrie Griffiths
 */
public class URLConnection {
  private InputStream in;
  private OutputStream out;
  private Socket socket;

  static final private char [] get = {'G','E','T',' '};
  static final private char [] http = {' ','H','T','T','P','/','1','.','0','\r','\n','\r','\n'};

  /**
   * Connects to a Socket via the proxy on the PC.
   * @param host the host to connect to
   * @param port the port to connect to
   * @param path the file to get 
   */
  public URLConnection(String host, int port, String path) throws IOException {

    // Open the socket

    socket = new Socket(host,port);
    in = socket.getInputStream();
    out = socket.getOutputStream();

    // Do an http get

    char [] pathChars = path.toCharArray();

    for(int i=0;i<get.length;i++)
      out.write((byte) get[i]);

    for(int i=0;i<pathChars.length;i++)
      out.write((byte) pathChars[i]);

    for(int i=0;i<http.length;i++)
      out.write((byte) http[i]);

    int done = 0;

    // Skip the headers

    do {
       int b = in.read();
       if (b == '\r') {
         if (done == 0 || done == 2) done++;
         else done = 0;
       } 
       else if (b == '\n') { 
         if (done == 1 || done == 3) done++;
         else done = 0;
       }
       else done = 0;
    } while (done < 4);
  }

  /** Returns an input stream for this URLConnection.
   * @return an input stream for reading bytes from this URLConnection.
   */
  public InputStream getInputStream() {
    return in;
  }

  /** Returns an output stream for this URLConnection.
   * @return an output stream for writing bytes to this URLConnection.
   */
  public OutputStream getOutputStream() {
    return out;
  }
}   
   

