
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
#include "systime.h"

#include <rom.h> // Requires librcx

static sensor_t sensors[3];

extern void reset_rcx_serial();

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
      push_word (sys_time);
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
      return;      
    case CALLROM1_V:
      __rcall1 (paramBase[0], paramBase[1]);
      return;      
    case CALLROM2_V:
      #if 0
      trace (-1, (TWOBYTES) paramBase[0], 6);
      trace (-1, (TWOBYTES) paramBase[1], 7);
      trace (-1, (TWOBYTES) paramBase[2] - 0xF010, 8);
      #endif
      __rcall2 (paramBase[0], paramBase[1], paramBase[2]);
      return;      
    case CALLROM3_V:
      __rcall3 (paramBase[0], paramBase[1], paramBase[2], paramBase[3]);
      return;
    case CALLROM4_V:
      __rcall4 (paramBase[0], paramBase[1], paramBase[2], paramBase[3], paramBase[4]);
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
    case RESETSERIAL_V:
      reset_rcx_serial();
      return;
    case READSENSORVALUE_I:
      // Parameters: int romId (0..2), int requestedValue (0..2).
      {
	short pId;
	
	pId = paramBase[0];
	if (pId >= 0 && pId < 3)
	{
          sensor_t *sensor;
	  
	  sensor = &(sensors[pId]);
	  read_sensor (0x1000 + pId, sensor);	  
	  switch ((byte) paramBase[1])
	  {
	    case 0:
	      push_word ((JINT) sensor->raw);
	      return;
	    case 1:
	      push_word ((JINT) sensor->value);
	      return;
	    case 2:
	      push_word (sensor->boolean);
	      return;
	  }
	}
      }
      push_word (0);
      return;
    case SETSENSORVALUE_V:
      // Arguments: int romId (1..3), int value, int requestedValue (0..3) 
      {
	short pId;
	
	pId = paramBase[0];
	if (pId >= 0 && pId < 3)
	{
          sensor_t *sensor;
	  STACKWORD value;
	  
	  value = paramBase[1];
	  sensor = &(sensors[pId]);
	  
	  switch ((byte) paramBase[2])
	  {
            case 0:
	      sensor -> mode = value;
	      return;
            case 1:	      
	      sensor -> type = value;
	      return;
	    case 2:
              sensor -> value = (short) (JINT) value;
	      return;
	    case 3:
              sensor -> boolean = value;
	      return;
	  }
	}
      }
      return;
    default:
      throw_exception (noSuchMethodError);
      return;
  }  
} 
