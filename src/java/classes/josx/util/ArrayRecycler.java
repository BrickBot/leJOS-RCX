package josx.util;

/**
 * An abstract array recycler. 
 * <p>
 * Note that the caller is expected to provide
 * thread safety for instances of this class.
 * 
 * @see josx.util.RecyclableArray
 */
public final class ArrayRecycler extends Recycler {
	private int requestedLength;
	
    /**
     * Constructs a recycler.
     * @param capacity Maximum number of allocated (non garbage) objects at any given time.
     */
    public ArrayRecycler() {
    }
    
    /**
     * Attempts to obtain a free RecyclableArray.
     * @return A RecyclableArray reference.
     * @throws java.lang.StackOverflowError May be thrown due to the recursive implementation of the method.
     */
    public final RecyclableArray allocate (int length) {
		RecyclableArray array1;
		this.requestedLength = length;
        array1 = (RecyclableArray) allocate();
		if (array1.getCapacity() >= length) {
			array1.init (length);
			return array1;
		}
		try {
		   return allocate (length);
		} finally {
		   // Must not recycle before calling allocate (length).
		   recycle (array1);
		}
	}	    

	protected final Recyclable createInstance() {
		return new RecyclableArray (this.requestedLength);
	}
}
