
#include "types.h"
#include "trace.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "stack.h"
#include "memory.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "exceptions.h"

/**
 * NOTE: The technique is not the same as that used in TinyVM.
 */
void dispatch_native (TWOBYTES signature, STACKWORD *paramBase)
{
  switch (signature)
  {
    case start_4_5V:
      init_thread ((Thread *) word2ptr(paramBase[0]));
      return;
    case yield_4_5V:
      switch_thread();
      return;
    case sleep_4J_5V:
      sleep_thread (paramBase[1]);
      switch_thread();
      return;
    case getPriority_4_5I:
      push_word (get_thread_priority ((Thread*)word2obj(paramBase[0])));
      return;
    case setPriority_4I_5V:
      {
        STACKWORD p = (STACKWORD)paramBase[1];
        if (p > MAX_PRIORITY || p < MIN_PRIORITY)
          throw_exception(illegalArgumentException);
        else
          set_thread_priority ((Thread*)word2obj(paramBase[0]), p);
      }
      return;
    case currentThread_4_5Ljava_3lang_3Thread_2:
      push_ref(ptr2ref(currentThread));
      return;
    case interrupt_4_5V:
      interrupt_thread((Thread*)word2obj(paramBase[0]));
      return;
    case interrupted_4_5Z:
      push_word(currentThread->interrupted);
      return;
    case isInterrupted_4_5Z:
      push_word(((Thread*)word2ptr(paramBase[0]))->interrupted);
      return;
    case setDaemon_4Z_5V:
      ((Thread*)word2ptr(paramBase[0]))->daemon = (JBYTE)paramBase[1];
      return;
    case isDaemon_4_5Z:
      push_word(((Thread*)word2ptr(paramBase[0]))->daemon);
      return;
    case exit_4I_5V:
      schedule_request(REQUEST_EXIT);
      return;
    case join_4_5V:
      join_thread((Thread*)word2obj(paramBase[0]));
      return;
    case currentTimeMillis_4_5J:
      push_word (0);
      push_word (0); // TBD
      return;
    case readMemoryByte_4I_5B:
      push_word ((STACKWORD) *((byte *) word2ptr(paramBase[0])));
      return;
    case writeMemoryByte_4IB_5V:
      *((byte *) word2ptr(paramBase[0])) = (byte) (paramBase[1] & 0xFF);
      return;
    case setMemoryBit_4III_5V:
      *((byte *)word2ptr(paramBase[0])) =
        ( *((byte *)word2ptr(paramBase[0])) & (~(1<<paramBase[1])) ) | (((paramBase[2] != 0) ? 1 : 0) <<paramBase[1]);
      return;      
    case getDataAddress_4Ljava_3lang_3Object_2_5I:
      push_word (ptr2word (((byte *) word2ptr (paramBase[0])) + HEADER_SIZE));
      return;
    case freeMemory_4_5J:
      push_word (0);
      push_word (getHeapFree());
      return;
    case totalMemory_4_5J:
      push_word (0);
      push_word (getHeapSize());
      return;
    case getRuntime_4_5Ljava_3lang_3Runtime_2:
      push_ref(ptr2ref(runtime));
      return;
    case assert_4Ljava_3lang_3String_2Z_5V:
      if (!paramBase[1])
      {
        throw_exception(error);
      }
      return;
    case assertEQ_4Ljava_3lang_3String_2II_5V:
      if (paramBase[1] != paramBase[2])
      {
        throw_exception(error);
      }
      return;
    default:
      throw_exception (noSuchMethodError);
      return;
  }  
} 
