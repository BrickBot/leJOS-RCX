package josx.platform.rcx;

/**
 * A memory area for persistent storage.
 * Only removing batteries will delete it.
 */

public class PersistentMemoryArea
{
  private static final int MAGIC_ADDRESS = 0xf010;
  private static final int SIZE_ADDRESS  = MAGIC_ADDRESS+2;
  private static final int START_ADDRESS = MAGIC_ADDRESS+4;
  private static final int END_ADDRESS   = 0xfd80;

  private static PersistentMemoryArea singleton = null;

  private int size;

  private PersistentMemoryArea ()
  {
  }

  public static PersistentMemoryArea get (int magic, int size)
    throws OutOfMemoryError  
  {
    if (singleton == null) {
      if (START_ADDRESS + size > END_ADDRESS) {
        throw new OutOfMemoryError();
      } else {
        if (Native.readMemoryShort (MAGIC_ADDRESS) != magic
            && Native.readMemoryShort (SIZE_ADDRESS) != size) {
          // not what we are looking for, need to reinitialize
          for (short i = 0; i < size; i++) {
            Native.writeMemoryByte (START_ADDRESS+i, (byte)0);
          }
          Native.writeMemoryShort (MAGIC_ADDRESS, (short)magic);
          Native.writeMemoryShort (SIZE_ADDRESS, (short)size);
        }
      }
      singleton = new PersistentMemoryArea ();
      singleton.size = size;
    }
    return singleton;
  }

  public byte readByte (int i)
    throws ArrayIndexOutOfBoundsException
  {
    if (i >= 0 && i < size) {
      return Native.readMemoryByte (START_ADDRESS+i);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  public void writeByte (int i, byte b)
    throws ArrayIndexOutOfBoundsException
  {
    if (i >= 0 && i < size) {
      Native.writeMemoryByte (START_ADDRESS+i, (byte)b);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }
}
