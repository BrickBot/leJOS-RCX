/**
 * poll.h
 * Contains conterparts of special classes as C structs.
 */
 
#include "classes.h"
#include "platform_hooks.h"

#ifndef _POLL_H

/**
 * Poll class native structure
 */
typedef struct S_Poll
{
  Object _super;	     // Superclass object storage
  JSHORT changed;            // Mask of inputs that have changed
} Poll;

extern void set_poller(Poll*);
extern void poll_inputs();
extern int throttle;
extern FOURBYTES next_poll_time;

// This really ough to be driven by interrupts from the
// hardware. For now just throttle it.
static inline void do_poll()
{
  // If throttle is 0 poll every time, if throttle
  // is 1 poll every millisecond etc.
  if (get_sys_time() >= next_poll_time)
  {
    next_poll_time = get_sys_time() + throttle;
    // Check sensors and buttons
    poll_hardware();
    // Actually just checks for program exit and on/off
    switch_thread_hook();
  }
}

#define _POLL_H
#endif // _POLL_H
