package java.lang;

public abstract class Thread
{
  // Note: The following fiels are used by the VM.
  // Their sizes and location can only be changed
  // if classes.h is changed accordingly. Needless
  // to say, they are read-only.

  private Thread _TVM_nextThread;
  private Object _TVM_waitingOn;
  private int _TVM_stackFrameArray;
  private int _TVM_stackArray;
  private int _TVM_currentStackFrame;
  private byte _TVM_threadId; 
  private byte _TVM_state; 

  // Extra instance state follows:
  
  private String name;

  // Static state follows:

  private static byte _TVM_threadIdCounter;
  private static Error _TVM_outOfMemoryError = new OutOfMemoryError();
  
  public Thread()
  {
    this ("");
  }

  public Thread (String name)
  {
    this.name = name;
    if (_TVM_threadIdCounter >= 63)
      _TVM_throwOutOfMemoryError();
    _TVM_threadId = ++_TVM_threadIdCounter;
  }
  
  static void _TVM_throwOutOfMemoryError()
  {
    throw _TVM_outOfMemoryError;
  }

  public native void start();
  public abstract void run();
}



