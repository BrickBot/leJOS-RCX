package javax.servlet.http;

import josx.rcxcomm.*;
import java.io.*;

public abstract class HttpServlet {

  private static final int MAX_PATH = 16;
  char [] path = new char[MAX_PATH];
  int pathLength = 0;

  public HttpServlet() throws IOException {
    RCXPort port = new RCXPort();
    InputStream is = port.getInputStream();
    OutputStream os = port.getOutputStream();
    HttpServletRequest req = new HttpServletRequest();
    HttpServletResponse resp = new HttpServletResponse(os); 

    while (true) {
      byte b = 0, lastB = 0, lastB2 = 0, lastB3;
      pathLength = 0;

      // Skip the get and leading slash

      for (int i= 0;i<5;i++) is.read();

      // Get the path

      for(;;) {
        b = (byte) is.read();
        if (b == ' ') break;
        path[pathLength++] = (char) b;
      } 

      req.setPath(path,pathLength);
 
      // Skip the request and headers

      do {
        lastB3 = lastB2;
        lastB2 = lastB;
        lastB = b;
        b = (byte) is.read();
      } 
      while (!(b == '\n' && lastB == '\r' && lastB2 == '\n' && lastB3 == '\r'));

      doGet(req, resp);

      // Trigger close Socket

      os.write((byte) 0xff);
    }
  }
  
  public abstract void doGet(HttpServletRequest request, HttpServletResponse response)
                            throws IOException;

}

    

    
      
