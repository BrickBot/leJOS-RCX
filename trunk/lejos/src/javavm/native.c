
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
      __rcall2 (paramBase[0], paramBase[1], paramBase[2]);
      break;      
    case CALLROM3_V:
      #if DEBUG_RCX
      debug (-1, paramBase[2], 9);
      #endif
      __rcall3 (paramBase[0], paramBase[1], paramBase[2], paramBase[3]);
      #if DEBUG_RCX
      debug (-1, paramBase[3], 8);
      #endif
      break;      
  }  
} 
