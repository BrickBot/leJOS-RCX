
// Sensor test

import tinyvm.rcx.*;

public class Test15
{ 
  public static void main (String[] argv)
  {
    for (int i = 0; i < 200; i++)
    {
      int pValue = Sensor.readSensorValue ((short) 0x1001, (byte) 3, (byte) 0x80);
      ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, pValue, 
                        ROM.LCD_POINT_DECIMAL_0);      
      ROM.setLcdNumber (ROM.LCD_CODE_PROGRAM, (short) (i % 10),
                        ROM.LCD_POINT_DECIMAL_0);      
      ROM.refreshLcd();
      for (int k = 0; k < 500; k++) { }
    }
  }
}

