#ifndef _MEMORY_H
#define _MEMORY_H

extern Object *new_object_for_class (byte classIndex);
extern Object *new_primitive_array (byte primitiveType, STACKWORD length);
extern void free_array (Object *objectRef);

#endif _MEMORY_H
