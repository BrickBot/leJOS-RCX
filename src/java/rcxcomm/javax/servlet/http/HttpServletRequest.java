package javax.servlet.http;

/**
 *
 * Provides request information for HTTP servlets. 
 *
 * The servlet container (the abstract HttpServlet on lejos), 
 * creates an <code>HttpServletRequest</code> 
 * object and passes it as an argument to the servlet's service
 * methods: (<code>doGet</code>).
 *
 *
 * @author 	Lawrie Griffiths
 *
 */
public class HttpServletRequest {
  private String path;
  private int pathLength;
  private String queryString;
  private int queryStringLength;
  private static final int MAX_PARAM = 16;
  private char [] tempChars = new char[MAX_PARAM];
  private String param = new String(tempChars, 0, MAX_PARAM);
  private char [] paramChars = StringUtils.getCharacters(param);
  private int paramLength = 0;
  
  void setPath(String path, int pathLength) {
    this.path = path;
    this.pathLength = pathLength;
  }

  void setQueryString(String queryString, int queryStringLength) {
    this.queryString = queryString;
    this.queryStringLength = queryStringLength;
  }

  /**
   * Returns the Servlet Path. 
   *
   * On lejos, this is the whole path up to the start of any query string    
   * @return  the servlet path
   */
  public String getServletPath() {
    return path;
  }

  /**
   * Returns the Request URI. 
   *
   * On lejos, this is the whole path up to the start of any query string.
   * It is the same as Servlet Path.
   *    
   * @return  the request URI
   */
  public String getRequestURI() {
    return path;
  }

  /**
   * Returns the real Servlet Path Length. 
   *
   * On lejos, servlet paths are padded with spaces to re-use Strings.
   * This extra method, returns the real length of the path.   
   * @return  the servlet path length
   */
  public int getServletPathLength() {
    return pathLength;
  }

  /**
   * Returns the Query String 
   *
   * This is the part of a URI after any "?".
   * It is all spaces is there is no Query String
   *    
   * @return  the request URI
   */
  public String getQueryString() {
    return queryString;
  }

  /**
   * Returns the real Query String Length. 
   *
   * On lejos, query strings are padded with spaces to re-use Strings.
   * This extra method, returns the real length of the query string.   
   * @return  the query string length
   */
  public int getQueryStringLength() {
    return queryStringLength;
  }

  /**
   * Returns the named parameter value, or null if there is no such parameter.
   * The returned string is re-used, so do not rely on it after a 
   * subsequent call of getParameter.
   *
   * @param name   The required parameter.
   *    
   * @return  the parameter value or null
   */
  public String getParameter(String name) {
    char [] nameChars = StringUtils.getCharacters(name);
    char [] queryStringChars = StringUtils.getCharacters(queryString);

    // Look for paameter name. Value must be at least one character

    for(int i=0;i<queryStringLength-nameChars.length-2;i++) {

      // Parameter name starts at beginning or after an ampersand

      if (!(i == 0 || queryStringChars[i-1] == '&')) continue;
      int j;
      for(j=0;j<nameChars.length;j++) {
        if (queryStringChars[i+j] != nameChars[j]) break;
      }

      // Parameter name must be followed by an equals sign

      if (j == nameChars.length && queryStringChars[j+i] == '=') {
        // Found the parameter
        int valueStart = i+j+1;

        // Copy value to paramChars, stop at end of wuery String or at an
        // ampersand or when length reaches MAX_PARAM

        for(paramLength=0;valueStart+paramLength<queryStringLength;paramLength++) {
          if (queryStringChars[valueStart+paramLength] == '&' ||
              paramLength == MAX_PARAM) break;
          paramChars[paramLength] = queryStringChars[valueStart+paramLength];
        }

        // Space fill the String

        for(j=paramLength;j<MAX_PARAM;j++) paramChars[j] = ' ';
        return param;
      }
    }

    // Return null if the parameter is not found

    return null;
  } 

  /**
   * Returns the real length of the current parameter. 
   *
   * On lejos, parameters are padded with spaces to re-use Strings.
   * This extra method, returns the real length of the parameter
   * value returned by the latest call of getParameter().
   *
   * @return  the parameter value length
   */
  public int getParameterLength() {
    return paramLength;
  }
}



