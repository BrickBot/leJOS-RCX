
#include "types.h"

#ifndef _INTERPRETER_H
#define _INTERPRETER_H

extern boolean gMustExit;
extern byte *pc;
extern STACKWORD *stackTop;
extern STACKWORD *localsBase;

extern void engine();

#endif
