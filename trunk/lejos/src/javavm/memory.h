#ifndef _MEMORY_H
#define _MEMORY_H

extern REFERENCE new_object_for_class (byte classIndex);
extern REFERENCE new_primitive_array (byte primitiveType, STACKWORD length);

#endif _MEMORY_H
