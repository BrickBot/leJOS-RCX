package java.lang;

/**
 * A thread of execution (or task).
 */
public abstract class Thread
{
  // Note 1: This class cannot have a static initializer.

  // Note 2: The following fiels are used by the VM.
  // Their sizes and location can only be changed
  // if classes.h is changed accordingly. Needless
  // to say, they are read-only.

  private Thread _TVM_nextThread;
  private Object _TVM_waitingOn;
  private Object _TVM_stackFrameArray;
  private Object _TVM_stackArray;
  private Object _TVM_isReferenceArray;
  private byte _TVM_stackFrameArraySize;
  private byte _TVM_threadId; 
  private byte _TVM_state; 

  // Extra instance state follows:
  
  private String name;

  public final boolean isAlive()
  {
    return _TVM_state != 0;
  }    
	  
  public Thread()
  {
    this ("");
  }

  public Thread (String name)
  {
    this.name = name;
  }

  public abstract void run();
  public final native void start();
  public static native void yield();
  public static native void sleep (long aMilliseconds) throws InterruptedException;
}



