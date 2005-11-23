package javax.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import josx.rcxcomm.RCXPort;

/**
 *
 * Provides an abstract class to be subclassed to create
 * an HTTP servlet suitable for a lejos Web Server. A subclass of
 * <code>HttpServlet</code> must override at least one method.
 *
 * The only method currently suported by lejos is:
 *
 * <ul>
 * <li> <code>doGet</code>, for HTTP GET requests
 * </ul>
 *
 * lejos currently supports a single Servlet. You call the parameterless
 * constructor to create the Servlet, and call th inti() method to
 * start the Web Server. It loops forever processing GET 
 * requests. 
 *
 * All paths are mapped onto this single servlet. 
 * 
 * @author	Lawrie Griffiths
 *
 **/
 public abstract class HttpServlet {

  private static final int MAX_PATH = 16;
  private static final int MAX_QUERY_STRING = 32;
  private static final int MAX_STRING = 32;

  private char [] tempChars = new char[MAX_STRING];

  private String path = new String(tempChars, 0, MAX_PATH);
  private String queryString = new String(tempChars, 0, MAX_QUERY_STRING);

  private char [] pathChars = StringUtils.getCharacters(path);
  private char [] queryStringChars = StringUtils.getCharacters(queryString);

  private int pathLength;
  private int queryStringLength;

  /**
   * Initialize the HTTP servlet 
   *
   * @exception IOException		if an input or output error occurs
   *
   **/
  public void init() throws IOException {
    RCXPort port = new RCXPort();
    InputStream is = port.getInputStream();
    OutputStream os = port.getOutputStream();
    HttpServletRequest req = new HttpServletRequest();
    HttpServletResponse resp = new HttpServletResponse(os); 

    while (true) {
      byte b = 0, lastB = 0, lastB2 = 0, lastB3;
      pathLength = 0;
      queryStringLength = 0;
      int i;

      port.reset(); // reset sequence numbers 

      // Skip the get 

      for (i= 0;i<4;i++) is.read();

      // Get the path

      for(i = 0;i<MAX_PATH;i++) {
        b = (byte) is.read();
        if (b == ' ' || b == '?') break;
        pathChars[pathLength++] = (char) b;
      } 

      while (b != ' ' && b != '?') b = (byte) is.read();

      // Pad with spaces

      for(i=pathLength;i < MAX_PATH;i++) pathChars[i] = ' ';

      if (b == '?') {
       
        // Get the query string
 
        for(i=0;i<MAX_QUERY_STRING;i++) {
          b = (byte) is.read();
          if (b == ' ') break;
          queryStringChars[queryStringLength++] = (char) b;
        } 
      }

      // Pad with spaces

      for(i=queryStringLength;i < MAX_QUERY_STRING;i++) queryStringChars[i] = ' ';

      // Set in request

      req.setPath(path,pathLength);
 
      req.setQueryString(queryString,queryStringLength);

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

  /**
   * doGet must be overridden to allow your Servlet to process GET requests
   * 
   *
   * @param request	the request object that is passed to the servlet
   * @param response the response object that the servlet uses to return the headers to the client
   * @exception IOException		if an input or output error occurs
   * @exception ServletException	not used in lejos version
   */
  public abstract void doGet(HttpServletRequest request, HttpServletResponse response)
                            throws IOException;

}

    

    
      
