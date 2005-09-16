package java.net;

import java.io.IOException;

/** An RCX version of URL that uses the RCX version of Socket.
 * Socket connects to TCP/IP ports via a 
 * proxy called SocketProxy running on the PC. It uses the 
 * josx.rcxcomm package.
 * The current version only deals with URLs in the form:
 * <protocol>://<host>/<path>, e.g. http://locahost/default.htm
 * It assumes a port of 80.
 * @author Lawrie Griffiths
 */
public class URL {
  private String path, host;
  private int port = 80;

  /**
   * Create a URL from the supplied string.
   * @param url the URL
   * @throws MalformedURLException
   */
  public URL(String url) throws MalformedURLException {
    char [] urlChars = url.toCharArray();
    int l = urlChars.length;
    int i = 0, hostStart, pathStart;

    // Isolate the protocol
    
    while (urlChars[i] != ':' && i<l) i++;
 
    if (i == l) throw new MalformedURLException();

    char [] protocolChars = new char[i];
    
    for(int j=0;j<i;j++) 
      protocolChars[j] = urlChars[j];

    // Isolate the host

    hostStart = i+3; // skip ://

    if (hostStart >= l) throw new MalformedURLException();
    
    i = hostStart;

    while(urlChars[i] != '/'&& i < l) i++;

    if (i == l) throw new MalformedURLException();

    char [] hostChars = new char[i-hostStart];

    for(int j=hostStart;j<i;j++) 
      hostChars[j-hostStart] = urlChars[j];

    host = new String(hostChars,0,i-hostStart);

    // Isolate the path

    pathStart = i;

    char [] pathChars = new char[l-pathStart];

    for(int j=pathStart;j<l;j++) 
      pathChars[j-pathStart] = urlChars[j];

    path = new String(pathChars,0,l-pathStart);
  }

  /**
   *  Create and open a URL connection using http get.
   * @throws IOxception
   */
  public URLConnection openConnection() throws IOException{
    return new URLConnection(host,port,path);
  }
}

    
    

    
    

    
