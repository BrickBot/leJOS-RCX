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
  // must be consistent with VM memory layout!
  private static final int REGION_HEADER_SIZE = 4;
  private static final int ARRAY_HEADER_SIZE  = 4;
  private static final int IS_ALLOCATED_MASK  = 0x8000;
  private static final int IS_ARRAY_MASK      = 0x4000;
  private static final int ELEM_TYPE_SHIFT    = 9;
  private static final int T_INT              = 10;
  private static final int INT_SIZE           = 4;

  private static final int REGION_ADDRESS       = 0xf002;
  private static final int BLOCK_HEADER_ADDRESS = REGION_ADDRESS +
                                                    REGION_HEADER_SIZE;
  private static final int MAGIC_ADDRESS = BLOCK_HEADER_ADDRESS + 
                                             ARRAY_HEADER_SIZE;
  private static final int SIZE_ADDRESS  = MAGIC_ADDRESS+2;
  private static final int START_ADDRESS = MAGIC_ADDRESS+4;

  private static PersistentMemoryArea singleton = null;

  private int size;

  private PersistentMemoryArea ()
  {
  }

  public static PersistentMemoryArea get (int magic, int size)
    throws OutOfMemoryError  
  {
    if (singleton == null) {
      synchronized (Memory.MONITOR) {
        short blockHeader = Memory.readShort ((short)BLOCK_HEADER_ADDRESS);
        // magic (2) + size (2) + round up (INT_SIZE-1)
        int arraySize = (size + 4 + INT_SIZE-1)/INT_SIZE; 
        int blockSize = ARRAY_HEADER_SIZE + INT_SIZE * arraySize;

        if ((blockHeader & IS_ALLOCATED_MASK) != 0 || // too late for us
            blockSize > blockHeader || // not enough space left
            arraySize > 511) {  // array would be too big
          throw new OutOfMemoryError();
        } else {
          // write header for following block
          Memory.writeShort (BLOCK_HEADER_ADDRESS+blockSize,
                             (short)(blockHeader-blockSize));
          // write array header
          blockHeader = (short)(IS_ARRAY_MASK | T_INT << ELEM_TYPE_SHIFT | arraySize);
          Memory.writeShort (BLOCK_HEADER_ADDRESS, blockHeader);
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
