// Stack overflow test

import josx.platform.rcx.*;

class BlowStackFrames extends Thread
{
  public void run()
  {
  	synchronized (Test14.monitor)
  	{
  		Test14.recurse();
    }
  }
}

class BlowLocals extends Thread
{
  public void recurse(int arg)
  {
  	int j = 0;
  	double d = 0.0;
  	float f = 0.0f;

	Test14.i++;
	recurse(3145-(arg+j)*0x20);
  }

  public void run()
  {
  	
  	synchronized (Test14.monitor)
  	{
  		recurse(Test14.i);
    }
  }
}

public class Test14
{ 
  static int i = 0;
  static Object monitor = new Object();
  
  public static void recurse()
  {
    	i++;
    	recurse();
  }

  public static void main (String[] argv)
  {
    new BlowStackFrames().start();
    try { Thread.sleep(100); } catch (InterruptedException ie) {}
    synchronized (monitor)
    {
    	// Wait for it to finish
    }
    LCD.showNumber (i);
    try { Thread.sleep(2000); } catch (InterruptedException ie) {}
    
    i=0;
    new BlowLocals().start();
    try { Thread.sleep(100); } catch (InterruptedException ie) {}
    synchronized (monitor)
    {
    	// Wait for it to finish
    }
    LCD.showNumber (i);
    try { Thread.sleep(2000); } catch (InterruptedException ie) {}    
  }
}

