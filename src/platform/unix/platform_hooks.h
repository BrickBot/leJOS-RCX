#ifndef _PLATFORM_HOOKS_H
#define _PLATFORM_HOOKS_H

// Methods declared here must be implemented by
// each platform.

#include "types.h"
#include "classes.h"
#include "language.h"
#include "interpreter.h"

#include "poll.h"

extern void poll_sensors();

extern int last_sys_time;
extern int last_ad_time;

static inline void instruction_hook()
{
  if( get_sys_time_impl() != last_sys_time){
    last_sys_time = get_sys_time_impl();
    gMakeRequest = true;
  }
}

static inline void tick_hook()
{
  if( get_sys_time_impl() >= last_ad_time + 3){
    last_ad_time = get_sys_time_impl();
    poll_sensors();
    poll_inputs();
  }
}

extern void switch_thread_hook();

/**
 * Called when thread is about to die due to an uncaught exception.
 */
extern void handle_uncaught_exception (Object *exception,
                                       const Thread *thread,
				       const MethodRecord *methodRecord,
				       const MethodRecord *rootMethod,
				       byte *pc);

/**
 * Dispatches a native method.
 */
extern void dispatch_native (TWOBYTES signature, STACKWORD *paramBase);				      

#endif // _PLATFORM_HOOKS_H




