package java.util;
/*
* $Log$
*/

/////////////////////////////////////////////////////////
/**
 * A FIFO Queue of objects. 
 */
public class Queue extends Vector {

	////////////////////////////////////////////
	// constants
	////////////////////////////////////////////
    
	////////////////////////////////////////////
	// fields
	////////////////////////////////////////////

	////////////////////////////////////////////
	// constructors
	////////////////////////////////////////////

	////////////////////////////////////////////
    /**
     * creates a new Queue instance
     */
    public Queue() {
    	// do nothing
    } // Queue()

	////////////////////////////////////////////
	/**
	 * pushes an object onto the Queue
	 * @param Object the object
	 * @return Object the object pushed onto the Queue
	 */
    public Object push(Object anObject) {
    	// add the object to base vector
		addElement(anObject);
		return anObject;
    } // push()

	////////////////////////////////////////////
	/**
	 * fetches an object from the start of the Queue
	 * and removes it
	 * @param Object the object
	 * @return Object the object removed from the start of the stock
	 * @throws EmptyQueueException
	 */
    public synchronized Object pop() throws EmptyQueueException {
		// get object
		Object popped = peek();
		// remove and return object
		removeElementAt(0);
		return popped;
    } // pop()

	////////////////////////////////////////////
	/**
	 * fetches an object from the start of the Queue
	 * <br>does not remove it!
	 * @param Object the object
	 * @return Object the object at the start of the Queue
	 * @throws EmptyQueueException
	 */
    public synchronized Object peek() throws EmptyQueueException {
		// empty Queue?
		if(size()==0)
	    	throw new EmptyQueueException();
	    // return first element
		return elementAt(0);
    } // peek()

	////////////////////////////////////////////
	/**
	 * is this Queue empty?
	 * @return boolean true, if the Queue is empty
	 */
    public boolean empty() {
		return (size()==0);
    } // empty()

} // class Queue
