
#ifndef _TRACE_H
#define _TRACE_H

#include "types.h"

#ifdef EMULATE

#include <stdio.h>

#define VERIFY 1

#endif EMULATE

#ifdef VERIFY

#define INTERPRETER0   10
#define INTERPRETER1   11
#define INTERPRETER2   12
#define INTERPRETER3   13
#define INTERPRETER4   14

#define THREADS0       20
#define THREADS1       21
#define THREADS2       22
#define THREADS3       23

#define MEMORY0        30
#define MEMORY1        31
#define MEMORY2        32
#define MEMORY3        33
#define MEMORY4        34
#define MEMORY5        35
#define MEMORY6        36
#define MEMORY7        37
#define MEMORY8        38

#define EXCEPTIONS0    40
#define EXCEPTIONS1    41

inline void assert (boolean aCond, int aCode)
{
  #ifdef EMULATE
  printf ("Assertion violation: %d\n", aCode);
  #else
  // TBD
  #endif
}

#endif // VERIFY

#endif _TRACE_H
