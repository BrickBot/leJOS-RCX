package josx.util;

/**
 * An abstract object recycler. This class should
 * be extended to define the createInstance
 * method for a particular kind of Recyclable.
 * <p>
 * Note that the caller is expected to provide
 * thread safety for instances of this class.
 * 
 * @see josx.util.Recyclable.
 */
public abstract class Recycler {
    private Recyclable firstInList;
	
    /**
     * Constructs a recycler.
     * @param capacity Maximum number of allocated (non garbage) objects at any given time.
     */
    public Recycler() {
    }
    
    /**
     * Attempts to obtain a free object.
     * @return A Recyclable object reference.
     */
    public final Recyclable allocate() {
        Recyclable f = this.firstInList;
		if (f != null) {
			this.firstInList = f.getNextRecyclable();
		} else {
		    f = createInstance();	
		}
		f.init();
		return f;
    }

	/**
	 * Reclaims a Recyclable previously allocated
	 * with the <code>allocate</code> method.
	 * The <code>release</code> method of the Recyclable object
	 * is invoked here.
	 */
	public final void recycle (Recyclable r) {
		r.release();
		r.setNextRecyclable (this.firstInList);
		this.firstInList = r;
	}
	
    /**
     * This is a factory method that should be
	 * overridden to create an Recyclable object instance.
	 */
	protected abstract Recyclable createInstance();
}
