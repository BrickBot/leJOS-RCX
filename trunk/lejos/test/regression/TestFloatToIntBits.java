import josx.util.Assertion;
import josx.platform.rcx.*;

public class TestFloatToIntBits{

  public static void main (String [] args) {
    float f = 1.23f;
    int i = Float.floatToIntBits(f);
    float newfloat = Float.intBitsToFloat(i);

    LCD.showNumber((int) (newfloat * 100.0f));

    Assertion.test("Value not 123", (int) (newfloat * 100.0f) == 123);

    try {
     Thread.sleep(5000);
    }
    catch (Exception e) {}
  }
}

    
