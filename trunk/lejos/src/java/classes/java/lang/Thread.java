package java.lang;

public abstract class Thread
{
  // Note 1: This class cannot have a static initializer.

  // Note 2: The following fiels are used by the VM.
  // Their sizes and location can only be changed
  // if classes.h is changed accordingly. Needless
  // to say, they are read-only.

  private Thread _TVM_nextThread;
  private Object _TVM_waitingOn;
  private int _TVM_stackFrameArray;
  private int _TVM_stackArray;
  private byte _TVM_stackFrameArraySize;
  private byte _TVM_threadId; 
  private byte _TVM_state; 

  // Extra instance state follows:
  
  private String name;

  public Thread()
  {
    this ("");
  }

  public Thread (String name)
  {
    this.name = name;
  }
  
  public native void start();
  public abstract void run();
}



