
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
#include "sensors.h"
#include "poll.h"
#include "llc.h"

extern void reset_rcx_serial();
extern char *mmStart;

/**
 * NOTE: The technique is not the same as that used in TinyVM.
 */
void dispatch_native (TWOBYTES signature, STACKWORD *paramBase)
{
  STACKWORD *paramBase1 = paramBase+1;
  STACKWORD *paramBase2 = paramBase+2;
  switch (signature)
  {
    case wait_4_5V:
      monitor_wait((Object*) word2ptr(paramBase[0]), 0);
      return;
    case wait_4J_5V:
      monitor_wait((Object*) word2ptr(paramBase[0]), *paramBase2);
      return;
    case notify_4_5V:
      monitor_notify((Object*) word2ptr(paramBase[0]), false);
      return;
    case notifyAll_4_5V:
      monitor_notify((Object*) word2ptr(paramBase[0]), true);
      return;
    case start_4_5V:
      init_thread ((Thread *) word2ptr(paramBase[0]));
      return;
    case sleep_4J_5V:
      sleep_thread (*paramBase1);
      // Drop through
    case yield_4_5V:
      schedule_request( REQUEST_SWITCH_THREAD);
      return;
    case getPriority_4_5I:
      push_word (get_thread_priority ((Thread*)word2obj(paramBase[0])));
      return;
    case setPriority_4I_5V:
      {
        STACKWORD p = (STACKWORD)*paramBase1;
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
      {
      	JBYTE i = currentThread->interruptState != INTERRUPT_CLEARED;
      	currentThread->interruptState = INTERRUPT_CLEARED;
      	push_word(i);
      }
      return;
    case isInterrupted_4_5Z:
      push_word(((Thread*)word2ptr(paramBase[0]))->interruptState
                != INTERRUPT_CLEARED);
      return;
    case setDaemon_4Z_5V:
      ((Thread*)word2ptr(paramBase[0]))->daemon = (JBYTE)*paramBase1;
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
    case join_4J_5V:
      join_thread((Thread*)word2obj(paramBase[0]));
      return;
    case currentTimeMillis_4_5J:
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
	arr2 = word2obj (*paramBase2);
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

    case call_4S_5V:
      __rcall0 (paramBase[0]);
      return;      
    case call_4SS_5V:
      __rcall1 (paramBase[0], *paramBase1);
      return;      
    case call_4SSS_5V:
      #if 0
      trace (-1, (TWOBYTES) paramBase[0], 6);
      trace (-1, (TWOBYTES) *paramBase1, 7);
      trace (-1, (TWOBYTES) *paramBase2 - 0xF010, 8);
      #endif
      __rcall2 (paramBase[0], *paramBase1, *paramBase2);
      return;      
    case call_4SSSS_5V:
      __rcall3 (paramBase[0], *paramBase1, *paramBase2, paramBase[3]);
      return;
    case call_4SSSSS_5V:
      __rcall4 (paramBase[0], *paramBase1, *paramBase2, paramBase[3], paramBase[4]);
      return;
    case readByte_4I_5B:
      push_word ((STACKWORD) *((byte *) word2ptr(paramBase[0])));
      return;
    case writeByte_4IB_5V:
      *((byte *) word2ptr(paramBase[0])) = (byte) (*paramBase1 & 0xFF);
      return;
    case setBit_4III_5V:
      *((byte *)word2ptr(paramBase[0])) =
        ( *((byte *)word2ptr(paramBase[0])) & (~(1<<*paramBase1)) ) | (((*paramBase2 != 0) ? 1 : 0) <<*paramBase1);
      return;      
    case getDataAddress_4Ljava_3lang_3Object_2_5I:
      push_word (ptr2word (((byte *) word2ptr (paramBase[0])) + HEADER_SIZE));
      return;
    case resetSerial_4_5V:
      reset_rcx_serial();
      return;
    case setPoller_4_5V:
      set_poller(word2ptr(paramBase[0]));
      return;
    case setThrottle_4I_5V:
      throttle = (byte)(paramBase[0]);
      return;
    case readSensorValue_4II_5I:
      // Parameters: int romId (0..2), int requestedValue (0..2).
      {
	short pId;
	
	pId = paramBase[0];
	if (pId >= 0 && pId < 3)
	{
          sensor_t *sensor;
	  
	  sensor = &(sensors[pId]);
	  switch ((byte) *paramBase1)
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
    case setSensorValue_4III_5V:
      // Arguments: int romId (1..3), int value, int requestedValue (0..3) 
      {
	short pId;
	
	pId = paramBase[0];
	if (pId >= 0 && pId < 3)
	{
          sensor_t *sensor;
	  STACKWORD value;
	  
	  value = *paramBase1;
	  sensor = &(sensors[pId]);
	  
	  switch ((byte) *paramBase2)
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
    case freeMemory_4_5J:
      push_word (0);
      push_word (getHeapFree());
      return;
    case totalMemory_4_5J:
      push_word (0);
      push_word (getHeapSize());
      return;
    case test_4Ljava_3lang_3String_2Z_5V:
      if (!*paramBase1)
      {
        throw_exception(error);
      }
      return;
    case testEQ_4Ljava_3lang_3String_2II_5V:
      if (*paramBase1 != *paramBase2)
      {
        throw_exception(error);
      }
      return;
    case floatToIntBits_4F_5I: // Fall through
    case intBitsToFloat_4I_5F:
      push_word (paramBase[0]);
      return;
    case init_4_5V:
      llc_init();
      return;
    case read_4_5I:
      push_word(llc_read());
      return;
    case write_4_1BI_5V:
      llc_writebytes((unsigned char *) (ptr2word (((byte *) 
        word2ptr (paramBase[0])) + HEADER_SIZE)), paramBase[1]);
      return;
    case isSending_4_5Z:
      push_word(llc_is_sending());
      return;
    case isSendError_4_5Z:
      push_word(llc_send_error());
      return;   
    case getRegionAddress_4_5I:
      push_word (getRegionAddress());
      return;
    default:
      throw_exception (noSuchMethodError);
      return;
  }  
} 
