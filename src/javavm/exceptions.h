
#include "types.h"
#include "classes.h"

#ifndef _EXCEPTIONS_H
#define _EXCEPTIONS_H

extern Object *outOfMemoryError;

extern void init_exceptions();
extern void throw_exception (Object *throwable);

#endif


