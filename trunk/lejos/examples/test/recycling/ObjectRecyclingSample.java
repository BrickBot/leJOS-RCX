import josx.platform.rcx.LCD;
import josx.platform.rcx.Sound;
import josx.util.*;

/*
* $Log$
*
*/

//////////////////////////////////////
/**
* represents a sample application for leJOS' object recycling
*
* @author Matthias Paul Scholz
* @version 1.0
*/
public class ObjectRecyclingSample {

	///////////////////////////////////////////
	// constants
	///////////////////////////////////////////

	///////////////////////////////////////////
	// fields
	///////////////////////////////////////////
	
    ///////////////////////////////////////////
    // construction
    ///////////////////////////////////////////

    ///////////////////////////////////////////
    /**
     * constructs an ObjectRecyclingSample
     */
    public ObjectRecyclingSample() {
    } // ObjectRecyclingSample()

	///////////////////////////////////////////
	// public methods
	///////////////////////////////////////////
	
	///////////////////////////////////////////
	/**
	 * main method
	 */
	public static void main(String[] args) {
		// create a sample
		ObjectRecyclingSample sample = new ObjectRecyclingSample();
		// and run it
		sample.run(); 
    } // main()
    
	///////////////////////////////////////////
	/**
	 * runs the sample 
	 */
	public void run() {
		
		// first let's look for the memory still free
		displayFreeMemory();
		
		// now create a recyler
		MyRecycler myRecycler = new MyRecycler();
				
		// first step: we create a number of our recyclable objects,
		// and look at our memory afterwards
		MyRecyclable myRecycable1 = (MyRecyclable)myRecycler.allocate();
		MyRecyclable myRecycable2 = (MyRecyclable)myRecycler.allocate();
		MyRecyclable myRecycable3 = (MyRecyclable)myRecycler.allocate();
		displayFreeMemory();
		
		// now mark the recyclables as recyclable 
		myRecycler.recycle(myRecycable1);
		myRecycler.recycle(myRecycable2);
		myRecycler.recycle(myRecycable3);
		
		// second step: again we create a number of our recyclable objects,
		// this should consume no new memory because the recycled
		// recyclables are used
		MyRecyclable myRecycable4 = (MyRecyclable)myRecycler.allocate();
		MyRecyclable myRecycable5 = (MyRecyclable)myRecycler.allocate();
		MyRecyclable myRecycable6 = (MyRecyclable)myRecycler.allocate();
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

	///////////////////////////////////////////
	/**
	 * inner class: a Recyclable
	*/
	public class MyRecyclable extends AbstractRecyclable {  

		////////////////////////////////////////////
		// fields
		////////////////////////////////////////////
		private double[] fValues = null;

		////////////////////////////////////////////
		// construction
		////////////////////////////////////////////
		
		////////////////////////////////////////////
		/**
		 * constructs a Recyclable
		 */
		public MyRecyclable(int aSize) { 
			// create values
			fValues = new double[aSize];
		} // MyRecyclable()
		
		////////////////////////////////////////////
		// abstract AbstractRecyclable methods
		////////////////////////////////////////////

		////////////////////////////////////////////
		/**
		* Initializes the Recyclable
		*/
		public void init() {
			// nothing to do here
		} // init()

		////////////////////////////////////////////
		/**
		* Called by users when this Recyclable is no longer needed
		*/
		public void release() {
			// no resources to release here
		} // release()

	} // class MyRecyclable
	
	///////////////////////////////////////////
	/**
	 * inner class: a Recycler for MyRecyclable objects
	*/
	public class MyRecycler extends Recycler {  

		////////////////////////////////////////////
		// abstract Recycler methods
		////////////////////////////////////////////

		////////////////////////////////////////////
		/**
		* creates a MyRecycable instance
		*/
		public Recyclable createInstance() {
			return new MyRecyclable(10);
		} // createInstance()

	} // class MyRecycler
	
} // class ObjectRecyclingSample
