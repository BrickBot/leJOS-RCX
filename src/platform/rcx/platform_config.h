#ifndef _PLATFORM_CONFIG_H
#define _PLATFORM_CONFIG_H

#include "systime.h"

// Basic types

typedef unsigned char byte;
typedef signed char JBYTE;
typedef signed short JSHORT;
typedef signed long JINT;
typedef unsigned short TWOBYTES;
typedef unsigned long FOURBYTES;

// Converting words to pointers

#define ptr2word(PTR_)  ((STACKWORD) (TWOBYTES) (PTR_))
#define word2ptr(WRD_)  ((void *) (TWOBYTES) (WRD_))

// Macro to get 4-byte system time, used in sleep.

#define get_sys_time()  (sys_time)

// Byte order: Most significant byte goes first in the RCX

#define LITTLE_ENDIAN 0

// Floating point arithmetic supported?

#define FP_ARITHMETIC 1

// Are we using the timer IRQ to switch threads? Not yet.

#define PLATFORM_HANDLES_SWITCH_THREAD 0
#define TICKS_PER_TIME_SLICE          20

// sensors

#define ANGLE_DOUBLE_CHECK 1

#endif // _PLATFORM_CONFIG_H
