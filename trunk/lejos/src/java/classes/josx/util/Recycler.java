package josx.util;

/**
 * An abstract object recycler. This class can
 * be extended to define the createInstance
 * method.
 */
public abstract class Recycler {
    private final Recyclable[] objects;
	private static final Error MEMORY_ERROR = new OutOfMemoryError();

    /**
     * Constructs a recycler.
     * @param capacity Maximum number of allocated (non garbage) objects at any given time.
     */
    public Recycler (int capacity) {
	    objects = new Recyclable[capacity];    
    }
    
    /**
     * Attempts to obtain a free object.
     * @return A Recyclable object reference.
     * @throws java.lang.OutOfMemoryError If the capacity of the Recycler is exceeded
     */
    public Recyclable allocate() {
		Recyclable[] array = objects;
		int len = array.length;
		for (int i = 0; i < len; i++) {
			Recyclable r = array[i];
			if (r == null) {
				r = createInstance();
				array[i] = r;
			}
			if (r.isGarbage()) {
				r.init();
			}
			return r;
		}
	    throw MEMORY_ERROR;
    }

    /**
     * This is a factory method that should be
	 * overridden to create an Recyclable object instance.
	 */
	protected abstract Recyclable createInstance();
}
