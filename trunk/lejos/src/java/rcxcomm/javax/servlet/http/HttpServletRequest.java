package javax.servlet.http;

import java.io.*;

public class HttpServletRequest {
  char [] path;
  int pathLength;

  public void setPath(char [] path, int pathLength) {
    this.path = path;
    this.pathLength = pathLength;
  }

  public final char [] getServletPath() {
    return path;
  }

  public int getServeletPathLength() {
    return pathLength;
  }
}


