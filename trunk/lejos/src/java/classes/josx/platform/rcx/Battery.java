/**
 * RCX access classes.
 */
package josx.platform.rcx;

/**
 * Provides access to Battery.
 */
public class Battery
{
  /*
   * LEGO firmware calculates raw * 43988 / 1560 to get mV
   */
  private static final float VOLT_PER_INTERNAL_UNIT = (float)(43.988/1560.0);

  /**
   * @return Battery voltage.
   */
  public static int getVoltageInternal()
  {
    synchronized (Memory.MONITOR)
    {
      int pAddr = Memory.iAuxDataAddr;
      ROM.call ((short) 0x29f2, (short) 0x4001, (short) pAddr);
      return Memory.readShort (pAddr);
    }
  }

  /**
   * @return Battery voltage in Volt.
   */
  public static float getVoltage()
  {
    return VOLT_PER_INTERNAL_UNIT * Battery.getVoltageInternal();
  }
}
