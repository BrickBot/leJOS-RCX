
// Test for arrays

import tinyvm.rcx.ROM;

public class Test05
{
  public static void main (String[] aArg)
  {
    int[] pTones = new int[] { 1000, 1000, 1400, 1800, 1000, 600, 
			       1400, 1000, 200 };
    short[] pDurations = new short[] { 100, 50, 50, 100, 100, 100, 
			       50, 50, 100 };

    for (int i = 0; i < pTones.length; i++)
    {
      ROM.playTone (pTones[i], pDurations[i]);
    }
  }
}


