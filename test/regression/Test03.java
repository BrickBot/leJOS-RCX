
// Basic looping

import tinyvm.rcx.ROM;

public class Test03
{
  public static void main (String[] aArg)
  {
    int k;
    for (k = 10; k <= 100; k += 5)
    {
      ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                        ROM.LCD_POINT_DECIMAL_0);
    }
    while (k-- > 95)
    {
      ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k,
                        (short) (ROM.LCD_POINT_DECIMAL_0 + 1));
    }
    do
    {
      ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k,
                        (short) (ROM.LCD_POINT_DECIMAL_0 + 1));
      k++;
    } while (k < 100);
    ROM.refreshLcd();    
  }
}

