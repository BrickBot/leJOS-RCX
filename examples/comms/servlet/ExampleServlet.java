import java.io.*;
import javax.servlet.http.*;
import josx.platform.rcx.*;

/**
 * An Example Servlet that returns the value of Sensor 2.
 * If the request URI starts with "w", it also displays "Welcome from England".
 * Change this to whatever country the server is running in.
 */
public class ExampleServlet extends HttpServlet implements SensorConstants {
  private OutputStream out;

  private static String html1 = "<HTML><BODY>";
  private static String html2 = "<P>Welcome from England</P>"; 
  private static String html3 = "<P>S2: "; 
  private static String html4 = "</P></BODY></HTML>"; 
  private static String type = "text/html";

  private static int hits = 0;

  public void doGet (HttpServletRequest request,
                     HttpServletResponse response) 
              throws IOException {

    out = response.getOutputStream();
    String path = request.getServletPath();

    // set content type

    response.setContentType(type);

    // Write the HTML

    println(html1);

    // Only output the Welcome if the path name starts with w

    if (path.charAt(1) == 'w') println(html2);

    println(html3);
    
    // println(Integer.toString(Sensor.S2.readValue()));

    int val = Sensor.S2.readValue();

    out.write ('0' + (val/10));
    out.write ('0' + (val % 10));

    println(html4); 

    LCD.showNumber(++hits);
    // LCD.showNumber((int) System.getRuntime().freeMemory());
  }

  void println(String s) throws IOException {
    for(int i=0;i<s.length();i++) out.write((byte) s.charAt(i));
  }

  public static void main(String [] args) throws IOException {
    Sensor.S2.setTypeAndMode(SensorConstants.SENSOR_TYPE_LIGHT, 
                             SensorConstants.SENSOR_MODE_PCT);
    Sensor.S2.activate();

    ExampleServlet example = new ExampleServlet();
    example.init();
  }
}

