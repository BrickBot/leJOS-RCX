
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
    //    case _TVM_MAIN_METHOD_V:
    //  classRecord = get_class_record (ENTRY_CLASS);
    //  dispatch_special (classRecord, find_method (classRecord, MAIN_V),
    //                    retAddr);
    //  return;
    case START_V:
      init_thread ((Thread *) word2ptr(paramBase[0]));
      return;
    case CALLROM0_V:
      __rcall0 (paramBase[0]);
      break;      
    case CALLROM3_V:
      __rcall3 (paramBase[0], paramBase[1], paramBase[2], paramBase[3]);
      break;      
  }  
} 
