package josx.util;

/**
 * Represents a recyclable object.
 * @see josx.util.Recycler
 */
public abstract class AbstractRecyclable {
    private boolean isGarbage = true;
	
	/**
	 * Initializes the Recyclable. This method should
	 * be invoked from any overriding methods.
	 */
	public void init() {
		isGarbage = false;
	}
	
	/**
	 * Called by users when this Recyclable is no longer needed.
	 * This method should
	 * be invoked from any overriding methods.
	 */
	 public void release() {
		 isGarbage = true;
	 }
	
	/**
	 * Determines whether this Recyclable is garbage or not.
	 */
	 public final boolean isGarbage() {
		 return isGarbage;
	 }
}
