
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
#include "systime.h"

#include <rom.h>

extern void reset_rcx_serial();

void dispatch_native (TWOBYTES signature, STACKWORD *paramBase)
{
  switch (signature)
  {
    case START_V:
      if (!init_thread ((Thread *) word2ptr(paramBase[0])))
	return;
      break;
    case YIELD_V:
      // Pop current stack frame
      do_return (0);
      // Switch current thread
      switch_thread();
      // Go back and continue
      return;
    case SLEEP_V:
      sleep_thread (paramBase[1]);
      do_return (0);
      switch_thread();
      return;
    case CURRENTTIMEMILLIS_J:
      *(++stackTop) = 0;
      *(++stackTop) = sys_time;
      do_return (2);
      return;

#if 0

      case ARRAYCOPY_V:
      {
	Object *arr1;
	Object *arr2;
	byte    elemSize = 0;
	
	arr1 = word2obj (paramBase[0]);
	arr2 = word2obj (paramBase[2]);
	if (arr1 == JNULL || arr2 == JNULL)
	{
	  throw_exception (nullPointerException);
	  return;
	}
        if (!is_array (arr1) || !is_array (arr2) ||
	    (elemSize = get_element_size (arr1)) != get_element_size (arr2))
	{
	  throw_exception (classCastException);
	  return;
	}
	
	  
	###
      }
      break;

#endif

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
    case CALLROM4_V:
      __rcall4 (paramBase[0], paramBase[1], paramBase[2], paramBase[3], paramBase[4]);
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
    case SETMEMORYBIT_V:
      *((byte *)word2ptr(paramBase[0])) =
        ( *((byte *)word2ptr(paramBase[0])) & (~(1<<paramBase[1])) ) | (((paramBase[2] != 0) ? 1 : 0) <<paramBase[1]);
      break;      
    case GETDATAADDRESS_I:
      *(++stackTop) = ptr2word (((byte *) word2ptr (paramBase[0])) + HEADER_SIZE);
      do_return (1);
      return;
    case RESETSERIAL_V:
      reset_rcx_serial();
      break;
    default:
      throw_exception (noSuchMethodError);
      return;
  }  
  do_return (0);
} 
