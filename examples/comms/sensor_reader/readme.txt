This is an example using josx.rcxcomm.RCXBean with a JSP page.

1. Install JSP server. A few free ones are:
Resin - http://www.caucho.com/products/resin/
Tomcat - http://jakarta.apache.org/tomcat/

2. compile SensorReader.java with lejosc.exe and upload to the RCX.

3. Make sure pcrcxcomm.jar is in the CLASSPATH environment variable.
e.g. set classpath=.;c:\lejos\lib\pcrcxcomm.jar

4. Copy read_sensor.jsp to the webpage directory. This directory is different for Resin/Tomcat.

5. Start the JSP server.

6. Open a web browser and browse to read_sensor.jsp.
e.g. http://localhost:8080/read_sensor.jsp

=====================
Brian Bagnall
www.mts.net/~bbagnall