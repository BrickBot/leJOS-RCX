
#include "types.h"
#include "trace.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "memory.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "exceptions.h"

#include <rom.h>

void dispatch_native (TWOBYTES signature, STACKWORD *paramBase)
{
  ClassRecord *classRecord;

  switch (signature)
  {
    case START_V:
      init_thread ((Thread *) word2ptr(paramBase[0]));
      return;
    case CALLROM0_V:
      __rcall0 (paramBase[0]);
      break;      
    case CALLROM1_V:
      __rcall1 (paramBase[0], paramBase[1]);
      break;      
    case CALLROM2_V:
      #if 0
      trace (-1, (TWOBYTES) paramBase[0], 6);
      trace (-1, (TWOBYTES) paramBase[1], 7);
      trace (-1, (TWOBYTES) paramBase[2] - 0xF010, 8);
      #endif
      __rcall2 (paramBase[0], paramBase[1], paramBase[2]);
      break;      
    case CALLROM3_V:
      __rcall3 (paramBase[0], paramBase[1], paramBase[2], paramBase[3]);
      break;
    case READMEMORYBYTE_B:
      #if 0
      trace (-1, (TWOBYTES) ((byte *) word2ptr(paramBase[0])) - 0xF010, 4);
      trace (-1, *((byte *) word2ptr(paramBase[0])), 5);
      #endif
      *(++stackTop) = (STACKWORD) *((byte *) word2ptr(paramBase[0]));
      #if 0
      trace (-1, *stackTop, 5);
      #endif
      do_return (1);
      return;
    case WRITEMEMORYBYTE_V:
      #if 0
      trace (-1, (TWOBYTES) ((byte *) word2ptr(paramBase[0])) - 0xF010, 3);
      trace (-1, paramBase[1] & 0xFF, 4);
      #endif
      *((byte *) word2ptr(paramBase[0])) = (byte) (paramBase[1] & 0xFF);
      break;
  }  
  do_return (0);
} 
