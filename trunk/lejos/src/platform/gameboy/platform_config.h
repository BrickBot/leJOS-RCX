#ifndef _PLATFORM_CONFIG_H
#define _PLATFORM_CONFIG_H

/* TBD */

/* Constructs not available in gbdk */

#define inline /*nop*/

/* Converting words to pointers */

#define ptr2word(PTR_)  ((STACKWORD) (TWOBYTES) (PTR_))
#define word2ptr(WRD_)  ((void *) (TWOBYTES) (WRD_))
#define get_sys_time()  (0) /*TBD*/

/* Basic types */

typedef signed char JBYTE;
typedef signed int JSHORT;
typedef signed long JINT;
typedef unsigned int TWOBYTES;
typedef unsigned long FOURBYTES;

/* Byte order: Most significant byte goes last? */

#define LITTLE_ENDIAN 0

/* Are we using the timer IRQ to switch threads? Not yet. */

#define PLATFORM_HANDLES_SWITCH_THREAD 0
#define OPCODES_PER_TIME_SLICE         128

/* No extra assertion code */

#undef VERIFY

#endif _PLATFORM_CONFIG_H
