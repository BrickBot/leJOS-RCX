package java.lang;

/**
 * Special bootstrap class.
 */
class Boot_Thread
extends Thread
{
  public Boot_Thread()
  {
    // Not called
  }

  public void run()
  {
    _TVM_mainMethod();
  }

  private native void _TVM_mainMethod();
}

