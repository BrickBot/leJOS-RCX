import java.io.*;
import javax.servlet.http.*;
import josx.platform.rcx.*;

public class ExampleServlet extends HttpServlet implements SensorConstants {
  OutputStream out;

  static final char [] html1 = {'<','H','T','M','L','>','<','B','O','D','Y','>'};
  static final char [] html2 = {'<','P','>','W','e','l','o','m','e',' ','f','r','o','m',' ','E','n','g','l','a','n','d','<','/','P','>'};
  static final char [] html3 = {'<','P','>','S','2',':',' '}; 
  static final char [] html4 = {'<','/','P','>','<','/','B','O','D','Y','>','<','/','B','O','D','Y','>'};
  static final char [] type = {'t','e','x','t','/','h','t','m','l'};

  static int hits = 0;

  public ExampleServlet() throws IOException {
    super();
  }

  public void doGet (HttpServletRequest request,
                     HttpServletResponse response) 
              throws IOException {
    out = response.getOutputStream();
    char [] path = request.getServletPath();

    // set content type

    response.setContentType(type);

    // Write the HTML

    println(html1);

    // Only ouput the Welcomeif the path name starts with w

    if (path[0] == 'w') println(html2);

    println(html3);
    
    // println(Integer.toString(Sensor.S2.readValue()));

    int val = Sensor.S2.readValue();

    out.write ('0' + (val/10));
    out.write ('0' + (val % 10));

    println(html4); 

    // LCD.showNumber(++hits);
    LCD.showNumber((int) System.getRuntime().freeMemory());
  }

  void println(char []ca) throws IOException {
    for(int i=0;i<ca.length;i++) out.write((byte) ca[i]);
  }

  public static void main(String [] args) throws IOException {
    Sensor.S2.setTypeAndMode(SensorConstants.SENSOR_TYPE_LIGHT, 
                             SensorConstants.SENSOR_MODE_PCT);
    Sensor.S2.activate();

    ExampleServlet example = new ExampleServlet();
  }
}

