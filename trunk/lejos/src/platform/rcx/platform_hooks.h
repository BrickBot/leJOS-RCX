#ifndef _PLATFORM_HOOKS_H
#define _PLATFORM_HOOKS_H

// Methods declared here must be implemented by
// each platform.

#include "types.h"
#include "classes.h"
#include "language.h"
#include "poll.h"

#include "sensors.h"

/**
 * Called before each bytecode instruction
 */
static inline void instruction_hook( void)
{
}

/**
 * Called after timer tick before next bytecode instruction
 */
extern char timerdata1[6];

static inline void tick_hook( void)
{
  if( (timerdata1[0] & 0x80) != 0){ /* first handler run flag set? */
    poll_sensors();

    /* See if the Poll instance is interested */
    poll_inputs();

    timerdata1[0] &= ~0x80;
  }
}

static inline void idle_hook()
{
}

/**
 * Called after thread switch
 */
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




