
#include "types.h"
#include "constants.h"
#include "classes.h"
#include "threads.h"
#include "specialclasses.h"
#include "specialsignatures.h"
#include "language.h"
#include "memory.h"
#include "interpreter.h"
#include "exceptions.h"
#include "configure.h"
#include "trace.h"
#include "magic.h"

#include <stdio.h>

extern UBYTE malloc_heap_start;
extern UINT8 lejos_code[];

/**
 * bootThread is a special global.
 */
Thread *bootThread;

#define	MALLOC_HEAP_END		(0xDFFF - 0x200)
#define MALLOC_HEAP_START	((UWORD)&malloc_heap_start)

#define HC_NONE            0
#define HC_SHUTDOWN_POWER  1
#define HC_EXIT_PROGRAM    2

#define HP_NO_PROGRAM      0
#define HP_INTERRUPTED     1
#define HP_NOT_STARTED     2

#ifdef VERIFY

void assert (boolean aCond, int aCode)
{
  // TBD
}

#endif

char hookCommand;

void handle_uncaught_exception (Object *exception,
                                       const Thread *thread,
				       const MethodRecord *methodRecord,
				       const MethodRecord *rootMethod,
				       byte *pc)
{
  printf ("################ Uncaught exception ###################\n");
  printf ("- method: %ld\n", (long) methodRecord->signatureId);
  printf ("- exception: %ld\n", (long) get_class_index(exception));
  printf ("- opcode offset: %ld\n", (long) pc - (long) get_code_ptr(methodRecord));
  printf ("- thread: %ld\n", (long) thread->threadId);
  printf ("- root method: %ld\n", (long) rootMethod->signatureId);
}

/**
 * This function is invoked by switch_thread
 * and the download loop.
 */
void switch_thread_hook()
{
}

void main()
{
  TWOBYTES size;

 LABEL_PROGRAM_STARTUP:

  install_binary (lejos_code);
  initialize_binary();
  
  // Initialize memory for object allocation.
  // This can only be done after initialize_binary().

  size = MALLOC_HEAP_END - MALLOC_HEAP_START;
  size /= 2;
  init_memory (&malloc_heap_start, size);

  // TBD: can't write to ROM image (class initialized flag)

  // Initialize special exceptions
  init_exceptions();

  // Create the boot thread (bootThread is a special global).
  bootThread = (Thread *) new_object_for_class (JAVA_LANG_THREAD);

  // Initialize the threading module.
  init_threads();

  // Start/prepare boot thread. Sets thread state to STARTED,
  // which in the case of bootThread, means main will be scheduled.
  if (!init_thread (bootThread))
  {
    // There isn't enough memory to even start the boot thread!!
    printf ("Not enough memory to even start!\n");
    // The program is useless now
    return;
  }

  // Execute the bytecode interpreter.
  set_program_number (0);
  engine();
}

