#include "poll.h"
#include "sensors.h"
#include "threads.h"

Poll *poller = 0;
short old_sensor_values[3];
short old_st;
int throttle = 1;
FOURBYTES next_poll_time;

void set_poller(Poll *_poller)
{
  byte i;
  
  poller = _poller;
  old_st = 0;
  for (i=0; i<3; i++)
    old_sensor_values[i] = sensors[i].value;
}

void poll_inputs()
{
  short changed = 0;
  short st = 0;    
  short i;
  short *pOldValue = old_sensor_values;
  sensor_t *pSensor = &sensors[0];

  // If we're not polling or somone already has the monitor
  // return.
  if (!poller || get_monitor_count((&(poller->_super))) != 0)
    return;

  // We do not have a thread that we can use to grab
  // the monitor but that's OK because we are atomic
  // anyway.
      
  // Check the sensor canonical values.
  for (i=1; i<=4; i <<= 1,pOldValue++,pSensor++)
  {
    if (*pOldValue != pSensor->value) {
      changed |= i;
      *pOldValue = pSensor->value;
    }
  }

  // Check the button status
  read_buttons (0x3000, &st);
  st <<= 3;	// Shift into poll position  
  changed |= st ^ old_st;
  old_st = st;
  
  // Only wake threads up if things have changed since
  // we last looked.    
  if (changed)
  {
    // Or in the latest changes. Some threads may not have
    // responded to earlier changes yet so we can't
    // just overwrite them.
    short jword = 0;
    store_word((byte*)(&jword), 2, changed);
    poller->changed |= jword;

#if DEBUG_POLL
  jword = get_word((byte*)&poller->changed, 2);
  printf("Poller: poller->changed = 0x%1X\n", jword);      
#endif
    
    // poller.notifyAll()
    monitor_notify_unchecked(&poller->_super, 1);
  }
}
