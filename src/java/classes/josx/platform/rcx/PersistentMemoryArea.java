package josx.platform.rcx;

/**
 * A memory area for persistent storage.
 * Only removing batteries will delete it.
 *
 * The magic number should be different for each application
 * (use a random integer).
 * At the moment there can be only one PersistentMemoryArea,
 * that will be reinitialized if you change magic.
 * This may change in the future, with more than one area and
 * magic used to distinguish them.
 */

public class PersistentMemoryArea
{
  private static final int MAGIC_ADDRESS = 0xf001;
  private static final int SIZE_ADDRESS  = MAGIC_ADDRESS+2;
  private static final int START_ADDRESS = MAGIC_ADDRESS+4;
  private static final int END_ADDRESS   = 0xfb80;

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
        synchronized (Memory.MONITOR) {
          if (Memory.readShort (MAGIC_ADDRESS) != magic
              && Memory.readShort (SIZE_ADDRESS) != size) {
            // not what we are looking for, need to reinitialize
            for (short i = 0; i < size; i++) {
              Memory.writeByte (START_ADDRESS+i, (byte)0);
            }
            Memory.writeShort (MAGIC_ADDRESS, (short)magic);
            Memory.writeShort (SIZE_ADDRESS, (short)size);
          }
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
      return Memory.readByte (START_ADDRESS+i);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  public void writeByte (int i, byte b)
    throws ArrayIndexOutOfBoundsException
  {
    if (i >= 0 && i < size) {
      Memory.writeByte (START_ADDRESS+i, (byte)b);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }
}
