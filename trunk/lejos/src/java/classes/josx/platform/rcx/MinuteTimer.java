/**
 * RCX access classes.
 */
package josx.platform.rcx;

/**
 * Provides access to Battery.
 */
public class MinuteTimer
{
  /**
   * Resets two-byte timer in the RCX.
   */
  public static void reset()
  {
    ROM.call ((short) 0x339a, (short) 0);
  }
}
