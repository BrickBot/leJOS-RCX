
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
    case START_V:
      init_thread ((Thread *) word2ptr(paramBase[0]));
      return;
    case YIELD_V:
      switch_thread();
      return;
    case SLEEP_V:
      sleep_thread (paramBase[1]);
      switch_thread();
      return;
    case CURRENTTIMEMILLIS_J:
      push_word (0);
      push_word (0); // TBD
      return;
    case READMEMORYBYTE_B:
      push_word ((STACKWORD) *((byte *) word2ptr(paramBase[0])));
      return;
    case WRITEMEMORYBYTE_V:
      *((byte *) word2ptr(paramBase[0])) = (byte) (paramBase[1] & 0xFF);
      return;
    case SETMEMORYBIT_V:
      *((byte *)word2ptr(paramBase[0])) =
        ( *((byte *)word2ptr(paramBase[0])) & (~(1<<paramBase[1])) ) | (((paramBase[2] != 0) ? 1 : 0) <<paramBase[1]);
      return;      
    case GETDATAADDRESS_I:
      push_word (ptr2word (((byte *) word2ptr (paramBase[0])) + HEADER_SIZE));
      return;
    default:
      throw_exception (noSuchMethodError);
      return;
  }  
} 
