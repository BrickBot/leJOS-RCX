package java.lang;

/**
 * Minimalist version of the standard Java Runtime class.
 * @author Paul Andrews
 */
public class Runtime {
	/**
	 * Private so no one but us can create one.
	 */
	private Runtime() {
	}
	
	/**
	 * Get the single instance of us.
	 */
	public static native Runtime getRuntime();
	
	/**
	 * Return the amount of free memory.on the heap
	 *
	 * @return the free memory in bytes
	 */
	public native long freeMemory();
	
	/**
	 * Return the size of the heap in bytes.
	 *
	 * @return the free memory in bytes
	 */
	public native long totalMemory();
}
