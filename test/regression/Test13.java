
// Memory overflow test

import tinyvm.rcx.*;

public class Test13
{ 
  public static void main (String[] argv)
  {
    int i = 0;
    try {
      for (;;)
      {
        i++;
        new Object();
      }
    } finally {
      LCD.showNumber (i);
      LCD.showProgramNumber (5);
      for (int k = 0; k < 50000; k++) {}
    }    
  }
}

