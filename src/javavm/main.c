
#include "classes.h"
#include "threads.h"
#include "specialclasses.h"
#include "specialsignatures.h"
#include "language.h"
#include "memory.h"
#include "interpreter.h"

// TBD: Garbage collection of these special objects?

Object *outOfMemoryError = null;

int main (void)
{
  Thread *bootThread;
  while (true)
  {
    // Download bytecodes and initialize memory
    wait_for_download();
    // Create the main thread
    bootThread = (Thread *) new_object_for_class (JAVA_LANG_THREAD);
    init_thread (bootThread);
    // Create other special objects
    outOfMemoryError = (Object *) new_object_for_class (JAVA_LANG_OUTOFMEMORYERROR);
    // Prepare invocation of the static initializer of System
    dispatch_special (JAVA_LANG_THREAD, find_method (JAVA_LANG_THREAD, _CLINIT__V));
    // Execute the bytecode interpreter
    engine();    
  }
  return 0;
}

