import josx.platform.rcx.LCD;
import josx.platform.rcx.Sound;
import josx.util.ArrayRecycler;
import josx.util.RecyclableArray;

/*
* $Log$
*
*/

//////////////////////////////////////
/**
* represents a sample application for leJOS' array recycling
*
* @author Matthias Paul Scholz
* @version 1.0
*/
public class ArrayRecyclingSample {

	///////////////////////////////////////////
	// constants
	///////////////////////////////////////////
	/**
	 * length of array
	 */
	private final int ARRAY_LENGTH = 20;

	///////////////////////////////////////////
	// fields
	///////////////////////////////////////////
	
    ///////////////////////////////////////////
    // construction
    ///////////////////////////////////////////

    ///////////////////////////////////////////
    /**
     * constructs an ArrayRecyclingSample
     */
    public ArrayRecyclingSample() {
    } // ArrayRecyclingSample()

	///////////////////////////////////////////
	// public methods
	///////////////////////////////////////////
	
	///////////////////////////////////////////
	/**
	 * main method
	 */
	public static void main(String[] args) {
		// create a sample
		ArrayRecyclingSample sample = new ArrayRecyclingSample();
		// and run it
		sample.run(); 
    } // main()
    
	///////////////////////////////////////////
	/**
	 * runs the sample
	 */
	public void run() {
		
		// first let's look for the memory currently used
		displayFreeMemory();
		
		// now create an array recyler
		ArrayRecycler recycler = new ArrayRecycler();
		// and create some Integer objects to store in arrays
		Integer[] objects = new Integer[ARRAY_LENGTH*2];
				
		// first step: we create an array
		// fill it with the first half of our objects
		// and look at our memory afterwards 
		RecyclableArray array = recycler.allocate(ARRAY_LENGTH);
		for(int i=0;i<ARRAY_LENGTH;i++) 
			 array.put(i,objects[i]);
		displayFreeMemory();
		
		// now mark the array as recyclable 
		recycler.recycle(array);
		
		// second step: again we allocate and fill a new array; 
		// this should consume no new memory because the recycled
		// array is used
		RecyclableArray recycledArray = recycler.allocate(ARRAY_LENGTH);
		for(int i=0;i<ARRAY_LENGTH;i++) 
			recycledArray.put(i,objects[ARRAY_LENGTH+i]);
		displayFreeMemory();
		
	} // run()
	
	///////////////////////////////////////////
	// private methods
	///////////////////////////////////////////
	
	///////////////////////////////////////////
	/**
	 * displays the free memory and waits a bit
	 */
	private void displayFreeMemory() {
		LCD.showNumber((int)System.getRuntime().freeMemory()/10);
		Sound.beep();
		try {
			Thread.sleep(1000);
		} catch(InterruptedException exc) {
			// do nothing
		} // catch
	} // memory()
	
	///////////////////////////////////////////
	// inner classes
	///////////////////////////////////////////

} // class ArrayRecyclingSample
