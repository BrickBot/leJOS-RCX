<%@ page contentType="text/html" language='java' import='josx.rcxcomm.*'%>
<jsp:useBean id='rcx' class='josx.rcxcomm.RCXBean' scope='application'>
  <jsp:setProperty name='rcx' property='comPort' value='usb'/>
</jsp:useBean>
<HTML>
<HEAD><TITLE>JSP Page</TITLE></HEAD>
<BODY>
<FORM>
  <SELECT name='sensorID'>
    <OPTION value='0'>Sensor 1
    <OPTION value='1'>Sensor 2
    <OPTION value='2'>Sensor 3
  </SELECT>
  <INPUT type='SUBMIT'>
</FORM><BR>
<%
  try {
    String sensorIDString = request.getParameter("sensorID");
    if (sensorIDString != null) {
      int sensorID = Integer.decode(sensorIDString).intValue();
      rcx.lock(request);
      rcx.send((byte)sensorID);
      int value = rcx.receiveInt();
      out.println("The RCX returned the following value from 'Sensor "+
        (sensorID+1)+"':&nbsp;&nbsp;<b>"+value+"</b>");
    }
  } catch (NumberFormatException e) {
    out.println("The value of <code>sensorID</code> is invalid.");
  } finally {
    rcx.free(request);
  }
%>
</body>
</html>