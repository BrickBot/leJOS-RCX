
// Sensor test

import tinyvm.rcx.*;

public class Test17
{ 
  public static void main (String[] argv)
  {
    for (int i = 0; i < 500; i++)
    {
      if ((i % 2) == 0)
        Sensor.S2.activate();
      else
        Sensor.S2.passivate();
      for (int k = 0; k < 50; k++) { }
      int pValue = Sensor.S2.readPercentage();
      if (pValue > 90)
        Motor.A.forward();
      else if (pValue < 50)
        Motor.A.stop();
      ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, pValue, 
                        ROM.LCD_POINT_DECIMAL_0);      
      ROM.setLcdNumber (ROM.LCD_CODE_PROGRAM, i % 10, (short) 0); 
      ROM.refreshLcd();
      for (int k = 0; k < 1000; k++) { }
    }
  }
}
