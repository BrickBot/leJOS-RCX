package josx.platform.rcx;

/**
 * Abstraction for an RCX button.
 * Example:<p>
 * <code><pre>
 *    Button.RUN.waitForPressAndRelease();
 *    Sound.playTone (1000, 1);
 * </pre></code>
 */
public class Button
{
  /**
   * The Run button.
   */
  public static final Button RUN = new Button (0x01);
  /**
   * The View button.
   */
  public static final Button VIEW = new Button (0x02);
  /**
   * The Prgm button.
   */
  public static final Button PRGM = new Button (0x04);
  
  /**
   * Array containing VIEW, PRGM and RUN, in that order.
   */
  public static final Button[] BUTTONS = { Button.RUN, Button.VIEW, Button.PRGM };
  
  private static final ButtonListenerThread LISTENER_THREAD = new ButtonListenerThread();

  private int iCode;
  private ButtonListener[] iListeners = new ButtonListener[4];
  private int iNumListeners;
  
  private Button (int aCode)
  {
    iCode = aCode;
  }

  /**
   * @return <code>true</code> if button is pressed, <code>false</code> otherwise.
   */
  public final boolean isPressed()
  {
    return (readButtons() & iCode) != 0;
  }

  /**
   * Wait until the button is released.
   */
  public final void waitForPressAndRelease() throws InterruptedException
  {
  	Poll poller = new Poll();
	do {
		poller.poll(iCode << Poll.BUTTON_MASK_SHIFT, 0);
	} while (isPressed());
  }

  /**
   * Adds a listener of button events. Each button can serve at most
   * 4 listeners.
   */
  public synchronized void addButtonListener (ButtonListener aListener)
  {
    if (!LISTENER_THREAD.isAlive())
    {
      // Hack: Force initialization of Native
      Native.getDataAddress (null);
      // Initialize each button
      VIEW.iNumListeners = 0;
      RUN.iNumListeners = 0;
      PRGM.iNumListeners = 0;      
      // Start thread
      LISTENER_THREAD.setDaemon(true);
	  LISTENER_THREAD.setPriority(Thread.MAX_PRIORITY);
      LISTENER_THREAD.start();
    }
    iListeners[iNumListeners++] = aListener;
    LISTENER_THREAD.addToMask(iCode);
  }

  /**
   * <i>Low-level API</i> that reads status of buttons.
   * @return An integer with possibly some bits set: 0x02 (view button pressed)
   * 0x04 (prgm button pressed), 0x01 (run button pressed). If all buttons 
   * are released, this method returns 0.
   */
  public static int readButtons()
  {
    synchronized (Native.MEMORY_MONITOR)
    {
      int pAddr = Native.iAuxDataAddr;
      Native.callRom ((short) 0x1fb6, (short) 0x3000, (short) pAddr);
      return Native.readMemoryShort (pAddr);
    }
  }

  static class ButtonListenerThread extends Thread
  {
  	int mask;
    Poll poller = new Poll();   
    boolean[] iPreviousValue = new boolean[3];
    
    private void call(int sid) {
    	Button button = BUTTONS[sid];
        synchronized (button){
			boolean newValue = button.isPressed();
    		int numListeners = button.iNumListeners;
    		for (int i = 0; i < numListeners; i++) {
	    		if (newValue)
	    			button.iListeners[i].buttonPressed (button);
	    		else
	    			button.iListeners[i].buttonReleased (button);
    		}
            iPreviousValue[sid] = newValue;
        }
    }

    public void addToMask(int id) {
    	mask |= id << Poll.BUTTON_MASK_SHIFT;
    	
    	// Interrupt the polling thread, not the current one!
    	interrupt();
    }
    
    public void run()
    {
		for (;;) {
		  	try  {
				int changed = poller.poll(mask, 0);
				
			  	if ((changed & Poll.RUN_MASK) != 0)
			  		call(0);
			  	if ((changed & Poll.VIEW_MASK) != 0)
			  		call(1);
			  	if ((changed & Poll.PRGM_MASK) != 0)
			  		call(2);
		  	} catch (InterruptedException ie) {
		  	}
	  	}
    }
  }
}

