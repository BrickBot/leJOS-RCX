
#include "classes.h"

#ifndef _MEMORY_H
#define _MEMORY_H

extern void init_memory (TWOBYTES offset);
extern void free_array (Object *objectRef);
extern Object *new_object_checked (byte classIndex, byte *btAddr);
extern Object *new_object_for_class (byte classIndex);
extern Object *new_primitive_array (byte primitiveType, STACKWORD length);
extern Object *new_multi_array (byte elemType, byte totalDimensions, byte reqDimensions);

#endif _MEMORY_H
