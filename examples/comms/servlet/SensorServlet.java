import java.io.*;
import javax.servlet.http.*;
import josx.platform.rcx.*;

/**
 * An example servlet that maniplulates RCX Sensors:
 *
 * /activate?Sensor=Sn activates the Sensor, where n = 1, 2 or 3
 * /passivate?Sensor=Sn passivates Sensor n
 * /setSensor?Sensor=n,Type=<type>,Mode=<mode> sets the Sensor Type and Mode 
 *                                   where <type>=light or touch
 *                                     and <mode>=pct or bool
 * /readSensor?Sensor=Sn returns the Sensor value 
 *
 **/
public class SensorServlet extends HttpServlet implements SensorConstants {
  private OutputStream out;

  private static String html1 = "<HTML><BODY><P>";
  private static String html2 = ": "; 
  private static String html3 = "activated";
  private static String html4 = "</P></BODY></HTML>"; 
  private static String html5 = "passivated";
  private static String html6 = "type and mode set";
  private static String type = "text/html";
  private static String sensorParam = "Sensor";

  private static int hits = 0;

  /**
   * Process the GET request
   */
  public void doGet (HttpServletRequest request,
                     HttpServletResponse response) 
              throws IOException {

    out = response.getOutputStream();
    String path = request.getServletPath();

    // set content type

    response.setContentType(type);

    // Write the HTML

    println(html1);

    try {

      // get The Sensor parameter value, and determine the sensor

      String sensorName = request.getParameter(sensorParam);
      int sensor = sensorName.charAt(1) - '1';

      if (sensorName != null) {

        // Show the sensor name

        println(sensorName);
        println(html2);

        // See what request it is

        if (path.charAt(1) == 'a') { // activate
          Sensor.SENSORS[sensor].activate();
          println(html3);
        } else if (path.charAt(1) == 'p') { // passivate
          Sensor.SENSORS[sensor].passivate();
          println(html5);
        } else if (path.charAt(1) == 'r') { // read Sensor
        
          int val = Sensor.SENSORS[sensor].readValue();

          // output the snsorvalue (maximum 2 digits)

          out.write ('0' + (val/10));
          out.write ('0' + (val % 10));
 
        } else if (path.charAt(1) == 's') { // set Type and Mode
          String modeString = request.getParameter("Mode");
          int mode = (modeString.charAt(0) == 'b' ? SENSOR_MODE_BOOL : SENSOR_MODE_PCT);
          String typeString = request.getParameter("Type");
          int type = (typeString.charAt(0) == 'l' ? SENSOR_TYPE_LIGHT : SENSOR_TYPE_TOUCH);

          Sensor.SENSORS[sensor].setTypeAndMode(type,mode);
          println(html6);       
        }
      }
    } catch (Exception e) {}

    println(html4); 

    LCD.showNumber(++hits);
    // LCD.showNumber((int) System.getRuntime().freeMemory());
  }

  private void println(String s) throws IOException {
    for(int i=0;i<s.length();i++) out.write((byte) s.charAt(i));
  }

  public static void main(String [] args) throws IOException {
    (new SensorServlet()).init();
  }
}

