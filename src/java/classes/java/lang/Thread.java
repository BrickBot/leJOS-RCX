package java.lang;

public abstract class Thread
{
  static
  {
    new Thread ("main") {
      public void run() {
        Thread._TVM_mainMethod();
      }
    }.start();
  }    

  // Note: The following fiels are used by the VM.
  // Their sizes and location can only be changed
  // if classes.h is changed accordingly.

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
  private static final OutOfMemoryError _TVM_outOfMemoryError =
    new OutOfMemoryError();
  
  public Thread()
  {
    this ("n/a");
  }

  public Thread (String name)
  {
    this.name = name;
    if (_TVM_threadIdCounter >= 63)
      throw _TVM_outOfMemoryError;
    _TVM_threadId = ++_TVM_threadIdCounter;
  }
  
  static void _TVM_throwOutOfMemoryError()
  {
    throw _TVM_outOfMemoryError;
  }

  public native void start();
  public abstract void run();
  private static native void _TVM_mainMethod();
}



