
// Simple anonymous threads

import tinyvm.rcx.*;

public class Test13
{ 
  public int iCounter;
  public final Object MONITOR = new Object();

  public void plainMethod()
  {
  }

  public static void main (String[] argv)
  {
    final Test13 pObj1 = new Test13();
    new Thread() {
      public void run() {
        pObj1.plainMethod();
      }
    }.start();
  }
}

