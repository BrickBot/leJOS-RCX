
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

void dispatch_native (TWOBYTES signature, STACKWORD *paramBase)
{
  ClassRecord *classRecord;

  switch (id)
  {
    //    case _TVM_MAIN_METHOD_V:
    //  classRecord = get_class_record (ENTRY_CLASS);
    //  dispatch_special (classRecord, find_method (classRecord, MAIN_V),
    //                    retAddr);
    //  return;
    case START_V:
      init_thread ((Thread *) paramBase[0]);
      return;
  }  
} 
