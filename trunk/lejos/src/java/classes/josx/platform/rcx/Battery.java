/**
 * RCX access classes.
 */
package josx.platform.rcx;

/**
 * Provides access to Battery.
 */
public class Battery
{
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
   * @return Battery voltage in mV.
   */
  public static int getVoltageMilliVolt()
  {
    /*
     * calculation from LEGO firmware
     */
    return Battery.getVoltageInternal() * 43988 / 1560;
  }

  /**
   * @return Battery voltage in Volt.
   */
  public static float getVoltage()
  {
    return (float)(Battery.getVoltageMilliVolt() * 0.001);
  }
}
