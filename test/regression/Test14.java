
// Stack overflow test

import tinyvm.rcx.*;

public class Test14
{ 
  static int i = 0;
  
  public static void recurse()
  {
    i++;
    recurse();
  }

  public static void main (String[] argv)
  {
    try {
      recurse();
    } finally {
      ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, i, ROM.LCD_POINT_DECIMAL_0);
      ROM.refreshLcd();
      for (int k = 0; k < 50000; k++) {}
    }    
  }
}

