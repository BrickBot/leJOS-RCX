
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

Thread *bootThread;

#ifdef VERIFY

extern void assert (boolean aCond, int aCode)
{
  // TBD
}

#endif VERIFY

int main (void)
{
  // TBD: !!!
  // Initialize memory
  init_memory (0);
  // Initialize exceptions
  init_exceptions();
  // Create the main thread
  bootThread = (Thread *) new_object_for_class (JAVA_LANG_THREAD);
  init_thread (bootThread);
  // Execute the bytecode interpreter
  engine();
  // Engine never returns?
  return 0;
}

