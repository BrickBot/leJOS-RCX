package josx.platform.rcx;

/**
 * Utiltity class for dispatching events to button and sensor listeners.
 *
 * @author Paul Andrews
 */
class ListenerThread extends Thread
{
  static ListenerThread singleton = new ListenerThread();
  
  int mask;
  Poll poller = new Poll();   

  static ListenerThread get()
  {
      synchronized (singleton)
      {
          if (!singleton.isAlive())
          {
            singleton.setDaemon(true);
            singleton.setPriority(Thread.MAX_PRIORITY);
            singleton.start();
          }
      }
      
      return singleton;
  }

  void addToMask(int mask)
  {
     this.mask |= mask;
      
      // Interrupt the polling thread, not the current one!
     interrupt();
  }

  void addButtonToMask(int id) {
      addToMask(id << Poll.BUTTON_MASK_SHIFT);
  }
  
  void addSensorToMask(int id) {
      addToMask(1 << id);
  }
  
  public void run()
  {
      for (;;) {
          try  {
              int changed = poller.poll(mask, 0);
              
              if ((changed & Poll.SENSOR1_MASK) != 0)
                  Sensor.S1.callListeners();
              if ((changed & Poll.SENSOR2_MASK) != 0)
                  Sensor.S2.callListeners();
              if ((changed & Poll.SENSOR3_MASK) != 0)
                  Sensor.S3.callListeners();
              if ((changed & Poll.RUN_MASK) != 0)
                  Button.RUN.callListeners();
              if ((changed & Poll.VIEW_MASK) != 0)
                  Button.VIEW.callListeners();
              if ((changed & Poll.PRGM_MASK) != 0)
                  Button.PRGM.callListeners();
          } catch (InterruptedException ie) {
          }
      }
  }
}
