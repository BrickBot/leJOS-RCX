
#include "classes.h"

#ifndef _MEMORY_H
#define _MEMORY_H

extern void init_memory (TWOBYTES offset);
extern void free_array (Object *objectRef);
extern Object *new_object_checked (byte classIndex, byte *btAddr);
extern Object *new_object_for_class (byte classIndex);
extern Object *new_primitive_array (byte primitiveType, STACKWORD length);
extern Object *new_multi_array (byte elemType, byte totalDimensions, byte reqDimensions);
extern STACKWORD make_word (byte *ptr, byte aSize);
extern void copy_word (byte *ptr, byte aSize, STACKWORD aWord);

#define HEADER_SIZE (sizeof(Object))
#define get_array_element_ptr(ARR_,ESIZE_,IDX_) ((byte *) (ARR_) + (IDX_) * (ESIZE_) + HEADER_SIZE)
#define get_array_word(ARR_,ESIZE_,IDX_)        (make_word (get_array_element_ptr(ARR_,ESIZE_,IDX_), ESIZE_))
#define set_array_word(ARR_,ESIZE_,IDX_,WRD_)   (copy_word (get_array_element_ptr(ARR_,ESIZE_,IDX_), ESIZE_, WRD_))

#endif _MEMORY_H
