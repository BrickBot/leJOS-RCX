import josx.platform.rcx.*;
import java.io.*;
import java.net.*;

/** 
 * A Browser for lejos!
 * Uses the josx.rcxcomm package to communicate with ServerProxy
 * on the PC. http headers are ignored, so redirects etc. do not work.
 * The text on the Web page is displayed as a marquee on the RCX 
 * display. This version display all text includes that is html
 * comments, <script> tags etc. 
 * @author Lawrie Griffiths
 */
class Browser {
  private static URL url;
  private static URLConnection urlc;
  private static final String page = "http://homepage.ntlworld.com/lawrie.griffiths/rcxbrowse.html";

  public static void main(String[] args) throws IOException {
    url = new URL(page);
    urlc = url.openConnection();
    InputStream in = urlc.getInputStream();
    char [] msg = new char[5];

    boolean tag = false;
    int i = 0;

    while (true) {
      int b = (char) in.read();
      if (b == '<') tag = true;
      else if (b == '>') {
        tag = false;
        continue;
      }
      if (tag) continue;
      if (i > 4) {
        TextLCD.print(msg);
        for(i=0;i<4;i++) msg[i] = msg[i+1];
        msg[i++] = (char) b;
      } else msg[i++] = (char) b;
    }
  }
}

   
