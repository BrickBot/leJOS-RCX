
// Hello-world program

import tinyvm.rcx.ROM;

public class Test2
{
  public static void main (String[] aArg)
  {
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) 4490, 
                      ROM.LCD_POINT_DECIMAL_0);
    ROM.refreshLcd();    
  }
}

