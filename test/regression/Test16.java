
// Sensor test

import tinyvm.rcx.*;

public class Test16
{ 
  private static byte counter = 0;

  public static byte readByte (short addr)
  {
    return 80;
  }

  public static int retvalue()
  {
    int b1 = 0;
    int b2 = readByte ((short) 0x100);
    return (b1 << 8) + b2;
  }

  public static void main (String[] argv)
  {
    for (int i = 0; i < 20; i++)
    {
      int pValue = retvalue();
      ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, pValue, 
                        ROM.LCD_POINT_DECIMAL_0);      
      ROM.refreshLcd();
      for (int k = 0; k < 1000; k++) { }
    }
  }
}
