
// Just a test for RCX tones

import tinyvm.rcx.ROM;

public class Test05
{
  public static void main (String[] aArg)
  {
    for (int i = 0; i < 100; i++)
    {
      ROM.playTone (i * 40 + 200, 12);
    }
  }
}


