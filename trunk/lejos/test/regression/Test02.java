
// Basic arithmatic

import tinyvm.rcx.ROM;

public class Test02
{
  public static void main (String[] aArg)
  {
    int i = 25;
    int j = -51;
    int k;
    k = i + j;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k = i * j;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k = j / i;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k = i - j;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k++;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k--;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    i = 0x37;
    j = 0xC4;
    k = i & j;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k = i | j;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k = i ^ j;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k = j >> 3;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    k = i << 3;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    ROM.refreshLcd();    
    byte ab = (byte) 220;
    k = (int) ab;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    ROM.refreshLcd();    
    k = ab & 0xFF;
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k, 
                      ROM.LCD_POINT_DECIMAL_0);
    ROM.refreshLcd();    
  }
}

