
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
