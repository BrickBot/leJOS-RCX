package josx.util;

/**
 * Represents a recyclable object.
 * @see josx.util.Recycler
 */
public interface Recyclable {
	/**
	 * Called when the Recyclable is allocated by a Recycler.
	 */
	public void init();
	
	/**
	 * Called by users when this Recyclable is no longer needed.
	 * Resources should be disposed in this method, e.g. this is where
	 * any nested Recyclables would be released.
	 */
	public void release();
	
	/**
	 * Determines whether this Recyclable is garbage or not.
	 * It should return true before the first time init() is
	 * invoked. It should return false after init() is invoked.
	 * It should return false after release() is invoked.
	 */
	public boolean isGarbage();
}
