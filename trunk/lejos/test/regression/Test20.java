
import josx.platform.rcx.*;

public class Test20
{
  native static void call (short aAddr);
  native static void call (short aAddr, short a1);
  native static void call (short aAddr, short a1, short a2);
  native static void call (short aAddr, short a1, short a2, short a3);

  native static byte readByte (short aAddr);
  native static void writeByte (short aAddr, byte aByte);

  public static void main (String[] arg)
  {
    int[] pData = new int[] {
      0x7c, 0xc8, 0x80, 0xe0, 0x80,
      0xF4, 0xF4, 0x00, 0x00, 0x00,
      0xF4, 0xF4, 0x00, 0x00, 0x00,
      0xF4, 0xF4, 0xFF, 0x00, 0x00
    };
    for (int i = 0; i < 18; i++)
      writeByte ((short) (0xef3e + i), (byte) pData[i]);
    //call1 ((short) 0x283c, (short) 0xef3e);
    LCD.refresh();
    for (int k = 0; k < 1000000; k++) { }
  }
}
