/**
 * nativeemul.c
 * Native method handling for unix_impl (emulation).
 */
#include <stdio.h>
#include <stdlib.h>
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
#include "platform_config.h"

static TWOBYTES gSensorValue = 0;

extern int	verbose;	/* If non-zero, generates verbose output. */
char *get_meaning(STACKWORD *);

void dump_flag(Object *obj)
{
  if (is_allocated(obj))
  {
    if (is_gc(obj))
    {
      printf("Ready for the garbage\n");
    }
    else if (is_array(obj))
    {
      printf("Array, type=%d, length=%d\n", get_element_type(obj), get_array_length(obj));
    }
    else
    {
      printf("Class index = %d\n", get_na_class_index(obj));
    }
  }
  else
  {
    printf ("Free block, length=%d\n", get_free_length(obj));
  }
  
  /**
   * Object/block flags.
   * Free block:
   *  -- bits 0-14: Size of free block in words.
   *  -- bit 15   : Zero (not allocated).
   * Objects:
   *  -- bits 0-7 : Class index.
   *  -- bits 8-12: Unused.
   *  -- bit 13   : Garbage collection mark.
   *  -- bit 14   : Zero (not an array).
   *  -- bit 15   : One (allocated).
   * Arrays:
   *  -- bits 0-8 : Array length (0-527).
   *  -- bits 9-12: Element type.
   *  -- bit 13   : Garbage collection mark.
   *  -- bit 14   : One (is an array).
   *  -- bit 15   : One (allocated).
   */

}
char* string2chp(String* s)
{
  char *ret = "null";
  if (s->characters)
  {
    int i;
    Object *obj;
    JCHAR *pA;
    int length;
    obj = word2obj(get_word((byte*)(&(s->characters)), 4));
    pA = jchar_array(obj);
    length = get_array_length(obj);
    ret = malloc(length+1);
    for (i=0; i<length; i++)
    {
      ret[i] = pA[i];
    }
    ret[i] = 0;
  }

  return ret;
}

/**
 * NOTE: The technique is not the same as that used in TinyVM.
 */
void dispatch_native (TWOBYTES signature, STACKWORD *paramBase)
{
	ClassRecord	*classRecord;

  switch (signature)
  {
    case wait_4_5V:
      monitor_wait((Object*) word2ptr(paramBase[0]), 0);
      return;
    case wait_4J_5V:
      monitor_wait((Object*) word2ptr(paramBase[0]), paramBase[2]);
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
    case yield_4_5V:
      switch_thread();
      return;
    case sleep_4J_5V:
      sleep_thread (paramBase[1]);
      switch_thread();
      return;
    case getPriority_4_5I:
      push_word (get_thread_priority ((Thread*)word2ptr(paramBase[0])));
      return;
    case setPriority_4I_5V:
      {
        STACKWORD p = (STACKWORD)paramBase[1];
        if (p > MAX_PRIORITY || p < MIN_PRIORITY)
          throw_exception(illegalArgumentException);
        else
          set_thread_priority ((Thread*)word2ptr(paramBase[0]), p);
      }
      return;
    case currentThread_4_5Ljava_3lang_3Thread_2:
      push_ref(ptr2ref(currentThread));
      return;
    case interrupt_4_5V:
      interrupt_thread((Thread*)word2ptr(paramBase[0]));
      return;
    case interrupted_4_5Z:
      {
      	JBYTE i = currentThread->interrupted;
      	currentThread->interrupted = 0;
      	push_word(i);
      }
      return;
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
    case join_4_5V:
      join_thread((Thread*)word2ptr(paramBase[0]));
      return;
    case join_4J_5V:
      join_thread((Thread*)word2obj(paramBase[0]));
      return;
    case exit_4I_5V:
      schedule_request(REQUEST_EXIT);
      return;
    case currentTimeMillis_4_5J:
      push_word (0);
      push_word (get_sys_time());
      return;
    case callRom_4S_5V:
	if(verbose == 0)
		printf ("& ROM call 0: 0x%lX\n", paramBase[0]);
	else
		printf("> %s\n", get_meaning(paramBase));
	return;      
    case callRom_4SS_5V:
	if(verbose == 0)
		printf ("& ROM call 1: 0x%lX (%ld)\n", paramBase[0], paramBase[1]);
	else
		printf("> %s\n", get_meaning(paramBase));
	return;      
    case callRom_4SSS_5V:
	if(verbose == 0)
		printf ("& ROM call 2: 0x%lX (%ld, %ld)\n", paramBase[0],
				paramBase[1], paramBase[2]);
	else
		printf("> %s\n", get_meaning(paramBase));
	return;      
    case callRom_4SSSS_5V:
	if(verbose == 0)
		printf ("& ROM call 3: 0x%lX (%ld, %ld, %ld)\n",
				paramBase[0], paramBase[1],
				paramBase[2], paramBase[3]);
	else
		printf("> %s\n", get_meaning(paramBase));
	return;      
    case callRom_4SSSSS_5V:
      printf ("& ROM call 4: 0x%lX (%ld, %ld, %ld, %ld)\n", paramBase[0],
                                                     paramBase[1],
                                                     paramBase[2],
                                                     paramBase[3],
                                                     paramBase[4]
             );
      return;      
    case readMemoryByte_4I_5B:
	if(verbose == 0)
		printf ("& Attempt to read byte from 0x%lX\n", (paramBase[0] & 0xFFFF));
	else
		printf ("> read byte from 0x%lX\n", (paramBase[0] & 0xFFFF));
	push_word (0);
	return;
    case writeMemoryByte_4IB_5V:
	if(verbose == 0)
		printf ("& Attempt to write byte [%lX] at 0x%lX (no effect)\n",
			paramBase[1] & 0xFF, paramBase[0] & 0xFFFF);
	else
		printf ("> write byte [%lX] at 0x%lX (no effect)\n",
			paramBase[1] & 0xFF, paramBase[0] & 0xFFFF);
      return;
    case setMemoryBit_4III_5V:
      printf ("& Attempt to set memory bit [%ld] at 0x%lX (no effect)\n", paramBase[1] & 0xFF, paramBase[0] & 0xFFFF);
      return;      
    case getDataAddress_4Ljava_3lang_3Object_2_5I:
      push_word (ptr2word (((byte *) word2ptr (paramBase[0])) + HEADER_SIZE));
      return;
    case resetSerial_4_5V:
      printf ("& Call to resetRcx");
      return;
    case readSensorValue_4II_5I:
      // Parameters: int romId (0..2), int requestedValue (0..2).
      if (gSensorValue > 100)
	gSensorValue = 0;
      push_word (gSensorValue++);
      return;
    case setSensorValue_4III_5V:
      // Arguments: int romId (1..3), int value, int requestedValue (0..3) 
      gSensorValue = paramBase[1];
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
        printf("%s\n",string2chp((String*)word2ptr(paramBase[0])));
        throw_exception(error);
      }
      return;
    case assertEQ_4Ljava_3lang_3String_2II_5V:
      if (paramBase[1] != paramBase[2])
      {
        printf("%s: expected %ld, got %ld\n",string2chp((String*)word2ptr(paramBase[0])), paramBase[1], paramBase[2]);
        throw_exception(error);
      }
      return;
    default:
#ifdef DEBUG_METHODS
      printf("Received bad native method code: %d\n", signature);
#endif
      throw_exception (noSuchMethodError);
  }
} 


