package javax.servlet.http;

import java.io.*;

public class HttpServletResponse {
  OutputStream os;
 
  public HttpServletResponse(OutputStream os) {
    this.os = os;
  }

  public OutputStream getOutputStream() {
    return os;
  }

  private static final char [] resp = {'H','T','T','P','/','1','.','0',' ','2','0','0','\r','\n',
                  'C','o','n','t','e','n','t','-','T','y','p','e',':',' '};
  private static final char [] crlf2 = {'\r','\n','\r','\n'};

  private void send(char [] ca) throws IOException {
    for(int i=0;i<ca.length;i++) os.write((byte) ca[i]); 
  }

  public void setContentType(char [] type) throws IOException {
    send(resp);
    send(type);
    send(crlf2);
  }
}
  
