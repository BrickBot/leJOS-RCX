#ifndef _PLATFORM_CONFIG_H
#define _PLATFORM_CONFIG_H

#include <asm/types.h>

/* TBD */

#define WIMPY_MATH 1

/* Constructs not available in gbdk */

#define inline   /*nop*/
#define register /*nop*/

/* Converting words to pointers */

#define ptr2word(PTR_)  ((STACKWORD) (TWOBYTES) (PTR_))
#define word2ptr(WRD_)  ((void *) (TWOBYTES) (WRD_))
#define get_sys_time()  (0) /*TBD*/
#define message(STR_)   printf(STR_)

/* Basic types */

typedef UINT8  byte;
typedef INT8   JBYTE;
typedef INT16  JSHORT;
typedef INT32  JINT;
typedef UINT16 TWOBYTES;
typedef UINT32 FOURBYTES;

/* Byte order: Most significant byte goes last? */

#define LITTLE_ENDIAN 1

/* Are we using the timer IRQ to switch threads? Not yet. */

#define PLATFORM_HANDLES_SWITCH_THREAD 0
#define OPCODES_PER_TIME_SLICE         128

/* No extra assertion code */

#undef VERIFY

// hardware polling (none here)

static inline void poll_hardware(){}

#endif // _PLATFORM_CONFIG_H
