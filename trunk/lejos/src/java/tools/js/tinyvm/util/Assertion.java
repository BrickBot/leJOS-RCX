package js.tinyvm.util;

/**
 * @deprecated
 */
public class Assertion
{
  /**
   * @deprecated
   */
  private static int iVerboseLevel;

  /**
   * @deprecated
   */
public static boolean iTrace = Boolean.getBoolean ("trace");

  /**
   * @deprecated
   */
  public static void setVerboseLevel (int aVerboseLevel)
  {
    iVerboseLevel = aVerboseLevel;
  }

  /**
   * @deprecated
   */
  public static int getVerboseLevel()
  {
    return iVerboseLevel;
  }

  /**
   * @deprecated
   */
  public static void test (boolean aCond)
  {
    if (!aCond)
    {
      System.err.println ("Assertion violation.");
      new Error().printStackTrace();
      System.exit(1);
    }
  }

  /**
   * @deprecated
   */
  public static void fatal (String aMsg)
  {
    System.err.println ("Fatal: " + aMsg);
    System.exit(1);
  }

  /**
   * @deprecated
   */
  public static void verbose (int aLevel, String aMsg)
  {
//    if (iVerboseLevel >= aLevel)
      System.out.println (aMsg);
  }

  /**
   * @deprecated
   */
  public static void trace (String aMsg)
  {
    if (iTrace)
      System.out.println ("(" + System.currentTimeMillis() + ") " + aMsg);
  }

  /**
   * @deprecated
   */
  public static boolean isVerbose (int aLevel)
  {
//    return iVerboseLevel >= aLevel;
    return true;
  }
}
