package java.util;
/*
* $Log$
*/

/////////////////////////////////////////////////////////
/**
 * A LIFO stack of objects. 
 * TODO recycable
 */
public class Stack extends Vector {

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
     * creates a new Stack instance
     */
    public Stack() {
    	// do nothing
    } // Stack()

	////////////////////////////////////////////
	/**
	 * pushes an object onto the stack
	 * @param Object the object
	 * @return Object the object pushed onto the stack
	 */
    public Object push(Object anObject) {
    	// add the object to base vector
		addElement(anObject);
		return anObject;
    } // push()

	////////////////////////////////////////////
	/**
	 * fetches an object from the top of the stack
	 * and removes it
	 * @param Object the object
	 * @return Object the object removed from the top of the stock
	 * @throws EmptyStackException
	 */
    public synchronized Object pop() throws EmptyStackException {
		// get object
		Object popped = peek();
		// remove and return object
		removeElementAt(size() - 1);
		return popped;
    } // pop()

	////////////////////////////////////////////
	/**
	 * fetches an object from the stack
	 * <br>does not remove it!
	 * @param Object the object
	 * @return Object the object at the top of the stack
	 * @throws EmptyStackException
	 */
    public synchronized Object peek() throws EmptyStackException {
    	// size of stack
		int	sizeOfStack = size();
		// empty stack?
		if(sizeOfStack==0)
	    	throw new EmptyStackException();
	    // return top element
		return elementAt(sizeOfStack-1);
    } // peek()

	////////////////////////////////////////////
	/**
	 * is this stack empty?
	 * @return boolean true, if the stack is empty
	 */
    public boolean empty() {
		return (size()==0);
    } // empty()

} // class Stack
