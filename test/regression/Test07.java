
// Test for virtual methods

import tinyvm.rcx.ROM;

public class Test07
{
  public Test07()
  {
    ROM.controlMotor ('A', 1, 1);
    for (int i = 0; i < 10000; i++) {}
    ROM.controlMotor ('A', 3, 1);
  }

  public void virtualMethod (int i)
  {
    ROM.setLcdNumber (ROM.LCD_CODE_UNSIGNED, (short) i, (short) 0x3002);
    ROM.refreshLcd();
  }

  public static interface TestInterface
  {
    public void callback();
  }

  public static class Inner extends Test07
  implements TestInterface
  {
    public Inner()
    {
      this (30);
    }

    public void callback()
    {
      ROM.controlMotor ('C', 1, 1);
      for (int i = 0; i < 10000; i++) {}
      ROM.controlMotor ('C', 3, 1); 
    }

    public Inner (int k)
    {
      super();
      ROM.playTone ((short) (k * 20), (short) 50);
    }

    public void virtualMethod (int i)
    {
      super.virtualMethod (i + 2);
      ROM.playTone ((short) i, (short) 200);
    }
  }

  public static void main (String[] aArg)
  {
    Test07 p = new Inner();
    if (p == null)
      throw new NullPointerException();
    TestInterface t = (TestInterface) p; 
    if (t == null)
      throw new RuntimeException();
    p.virtualMethod (1800);
    t.callback();
    for (int i = 0; i < 100000; i++);
  }
}


