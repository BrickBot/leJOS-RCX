
// Test for arrays

import tinyvm.rcx.ROM;

public class Test05
{
  public static void main (String[] aArg)
  {
    int[] pTones = new int[] { 1000, 1000, 1400, 1800, 1000, 600, 
			       1400, 1000, 200 };
    short[] pDurations = new short[] { 50, 25, 25, 50, 50, 50, 
			       25, 25, 200 };

    for (int i = 0; i < pTones.length; i++)
    {
      ROM.playTone (pTones[i], pDurations[i]);
    }
    for (int k = 0; k < 100000; k++) {}
  }
}

