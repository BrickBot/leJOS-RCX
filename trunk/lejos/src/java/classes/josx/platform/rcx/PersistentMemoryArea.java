package josx.platform.rcx;

/**
 * A memory area for persistent storage.
 * The memory persists between runs of a program. Downloading
 * a new program will trash the area (unless you're very lucky).
 * <P>
 * The magic number should be different for each application
 * (use a random integer).
 * <P>
 * At the moment there can be only one PersistentMemoryArea,
 * that will be reinitialized if you change the magic number.
 * This may change in the future, with more than one area and
 * magic number used to distinguish them.
 * <P>
 * In case you experience problems, the specific restrictions are
 * as follows (which might help you fix the problem):
 * <ol>
 * <li>There can be only one persistent area of memory.
 * <li>It will not survive an application reload unless it is exactly
 * the same application (which means the application is axactly the
 * same size, it uses the same magic number and the persistent area
 * is exactly the same size).
 * <li>You can call PersistentMemoryArea.get(magic, size) exactly once
 * in any one run of an application. If you call it more than once you
 * will get an out-of-memory error.
 * <li>There is a very remote chance that on the first run of an
 * application it will be falsely determined that the persistent area
 * already exists. For this to happen, the area of heap used will have
 * to contain an exact match for the magic number and size requested.
 * </ol>
 */

public class PersistentMemoryArea
{
  // must be consistent with VM memory layout!
  private static final int REGION_HEADER_SIZE = 2;
  private static final int ARRAY_HEADER_SIZE  = 4;
  private static final int IS_ALLOCATED_MASK  = 0x8000;
  private static final int IS_ARRAY_MASK      = 0x4000;
  private static final int ELEM_TYPE_SHIFT    = 9;
  private static final int T_INT              = 10;
  private static final int INT_SIZE           = 4;

  private static int regionAddress;
  private static int blockHeaderAddress;
  private static int magicAddress;
  private static int sizeAddress;
  private static int startAddress;

  static {
  	regionAddress      = getRegionAddress();
  	blockHeaderAddress = regionAddress + REGION_HEADER_SIZE;
  	magicAddress       = blockHeaderAddress + ARRAY_HEADER_SIZE;
  	sizeAddress        = magicAddress+2;
	startAddress       = sizeAddress+2;
  };

  private static PersistentMemoryArea singleton = null;

  private int size;

  private PersistentMemoryArea ()
  {
  }

  /**
   * Allocate a persistent array of 'size' bytes. If this is a new
   * array, the values are initialized to zero. 
   * @param magic a 2 byte integer used to idenitfy the specific memory area.
   * @param size the size in bytes. This should be in the range 0 thru 511.
   * @exception OutOfMemoryError not enoug memory to allocate the array.
   */
  public static PersistentMemoryArea get (int magic, int size)
    throws OutOfMemoryError  
  {
    if (singleton == null) {
      synchronized (Memory.MONITOR) {
        // magic (2) + size (2) + round up (INT_SIZE-1)
        
        // arraySize is the size in 4 byte words of the array.
        int arraySize = (size + 4 + INT_SIZE-1)/INT_SIZE;
        
        // blockSize is the size in 2 byte words of the block. 
        int blockSize = ARRAY_HEADER_SIZE / 2 + INT_SIZE * arraySize / 2;
        
        // blockHeader is the size in 2 byte words of this segment.
        short blockHeader = Memory.readShort ((short)blockHeaderAddress);

        if ((blockHeader & IS_ALLOCATED_MASK) != 0 || // too late for us
            blockSize > blockHeader || // not enough space left
            arraySize > 511) {  // array would be too big
          throw new OutOfMemoryError();
        } else {
          Memory.writeShort (blockHeaderAddress+2*blockSize,
                             (short)(blockHeader-blockSize));
          // write array header
          blockHeader = (short)(IS_ALLOCATED_MASK | IS_ARRAY_MASK | T_INT << ELEM_TYPE_SHIFT | arraySize);
          Memory.writeShort (blockHeaderAddress, blockHeader);
          if (Memory.readShort (magicAddress) != magic
              && Memory.readShort (sizeAddress) != size) {
            // not what we are looking for, need to reinitialize
            for (short i = 0; i < size; i++) {
              Memory.writeByte (startAddress+i, (byte)0);
            }
            Memory.writeShort (magicAddress, (short)magic);
            Memory.writeShort (sizeAddress, (short)size);
          }
        }
      }
      singleton = new PersistentMemoryArea ();
      singleton.size = size;
    }
    return singleton;
  }

  /**
   * Read the byte at index 'i'
   * @param i the index starting at 0.
   * @exception ArrayIndexOutOfBoundsException if the index is
   * out of bounds.
   */
  public byte readByte (int i)
    throws ArrayIndexOutOfBoundsException
  {
    if (i >= 0 && i < size) {
      return Memory.readByte (startAddress+i);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  /**
   * Write a byte at index 'i'
   * @param i the index starting at 0.
   * @param b the byte value.
   * @exception ArrayIndexOutOfBoundsException if the index is
   * out of bounds.
   */
  public void writeByte (int i, byte b)
    throws ArrayIndexOutOfBoundsException
  {
    if (i >= 0 && i < size) {
      Memory.writeByte (startAddress+i, (byte)b);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  /**
   * Returns the address of the start of the heap.
   */  
  private static native int getRegionAddress();
}
