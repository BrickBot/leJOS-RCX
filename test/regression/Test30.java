
import tinyvm.rcx.*;

public class Test30
{
  public static void main (String[] arg)
  {
    float f1 = 1.53f;
    float f2 = 3.45f;
    float f3 = (f1 + f2) * 100;
    LCD.showNumber ((short) f3);
    for (int k = 0; k < 50000; k++) { }
    double d1 = 1.5e+3;
    double d2 = 3.4e+3;
    double d3 = (d1 - d2) / 10;
    LCD.showNumber ((short) d3);
    for (int k = 0; k < 50000; k++) { }
  }
}
