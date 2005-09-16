package javax.servlet.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * Provides HTTP-specific functionality for sending a response. 
 *
 * The servlet container (the abstract HttpServer on lejos) 
 * creates an <code>HttpServletRequest</code> object
 * and passes it as an argument to the servlet's service method:
 * (<code>doGet</code>.
 *
 * 
 * @author	Lawrie Griffiths
 *
 */
  public class HttpServletResponse {
  private OutputStream os;

  /**
   * Creates the Servlet Response object    
   */
  public HttpServletResponse(OutputStream os) {
    this.os = os;
  }

  /**
   * Returns an OutputStream for writing the response    
   * @return  the OutputStream
   */
  public OutputStream getOutputStream() {
    return os;
  }

  private static String resp = "HTTP/1.0 200\r\nContent-Type: ";
  private static String crlf2 = "\r\n\r\n";

  private void send(String s) throws IOException {
    char [] ca = StringUtils.getCharacters(s);
    for(int i=0;i<ca.length;i++) os.write((byte) ca[i]); 
  }

  /**
   * Set the Content Type.
   * On lejos you must call this method, and you must
   * call it before writing anything to the output stream.
   *    
   * @param type the Mime type, usually text/html
   */
  public void setContentType(String type) throws IOException {
    send(resp);
    send(type);
    send(crlf2);
  }
}
  
