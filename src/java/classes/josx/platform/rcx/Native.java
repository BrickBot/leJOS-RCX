/**
 * RCX access classes.
 */
package josx.platform.rcx;

/**
 * Provides access to native routines.
 */
public class Native
{
  /**
   * Should be used for all native memory accesses.
   * @see josx.platform.rcx.ROM#readMemoryByte
   * @see josx.platform.rcx.ROM#writeMemoryByte
   */
  public static final Object MEMORY_MONITOR = new Object();
  
  static final byte[] iAuxData = new byte[7];
  static final int iAuxDataAddr = getDataAddress (iAuxData);

  native static void callRom (short aAddr);
  native static void callRom (short aAddr, short a1);
  native static void callRom (short aAddr, short a1, short a2);
  native static void callRom (short aAddr, short a1, short a2, short a3);
  native static void callRom (short aAddr, short a1, short a2, short a3, short a4);

  native static byte readMemoryByte (int aAddr);
  native static void writeMemoryByte (int aAddr, byte aByte);

  native static int  getDataAddress (Object obj);
  native static void setMemoryBit(int aAddr, int bit, int value);

  static int readMemoryShort (int aAddr)
  {
    int b1 = Native.readMemoryByte (aAddr) & 0xFF;
    int b2 = Native.readMemoryByte (aAddr + 1) & 0xFF;
    return (b1 << 8) + b2;
  }

  static void writeMemoryShort (int aAddr, short s)
  {
    Native.writeMemoryByte (aAddr, (byte)((s >> 8) & 0xff));
    Native.writeMemoryByte (aAddr + 1, (byte)(s & 0xff));
  }
}


