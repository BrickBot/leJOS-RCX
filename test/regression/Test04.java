
// Basic object creation and field access

import tinyvm.rcx.ROM;

public class Test04
{
  static Test04 iObj2;
  static int iStatic;
  byte iByteField;
  int iIntField;
  byte iByteField2;
  long iLongField;
  short iShortField;

  public static void main (String[] aArg)
  {
    Test04 pObj = new Test04();
    iObj2 = new Test04();
    Test04.iStatic = 4490; 
    pObj.iByteField = 100; 
    pObj.iIntField = 100000; 
    pObj.iByteField2 = 80; 
    pObj.iShortField = 4000;
    iObj2.iByteField = 125;
    iObj2.iShortField = 4175;
    int k = pObj.iStatic;   
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) k,
                      (short) (ROM.LCD_POINT_DECIMAL_0 + 1));
    Test04.iStatic = 4491;
    int lvar = pObj.iShortField + pObj.iByteField2 + pObj.iIntField + 
               pObj.iByteField;   
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) (lvar - 99999),
                      (short) (ROM.LCD_POINT_DECIMAL_0 + 1));
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) (iObj2.iByteField +
                      iObj2.iShortField),
                      (short) (ROM.LCD_POINT_DECIMAL_0 + 1));
  }
}


