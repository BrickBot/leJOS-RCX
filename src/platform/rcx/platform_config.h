#ifndef _PLATFORM_CONFIG_H
#define _PLATFORM_CONFIG_H

// Converting words to pointers

#define ptr2word(PTR_)  ((STACKWORD) (TWOBYTES) (PTR_))
#define word2ptr(WRD_)  ((void *) (TWOBYTES) (WRD_))

// Byte order: Most significant byte goes first in the RCX

#define LITTLE_ENDIAN 0

// Floating point arithmetic supported?

#define FP_ARITHMETIC 1

// Are we using the timer IRQ to switch threads? Not yet.

#define PLATFORM_HANDLES_SWITCH_THREAD 0
#define OPCODES_PER_TIME_SLICE         128

// No extra assertion code

#undef VERIFY

#endif _PLATFORM_CONFIG_H
