
#include "types.h"
#include "classes.h"

#ifndef _MEMORY_H
#define _MEMORY_H

extern void init_memory (void *ptr, TWOBYTES size);
extern void free_array (Object *objectRef);
extern Object *new_object_checked (byte classIndex, byte *btAddr);
extern Object *new_object_for_class (byte classIndex);
extern Object *new_primitive_array (byte primitiveType, STACKWORD length);
extern Object *new_multi_array (byte elemType, byte totalDimensions, byte reqDimensions);
extern void make_word (byte *ptr, byte aSize, STACKWORD *aWordPtr);
extern void save_word (byte *ptr, byte aSize, STACKWORD aWord);

#define HEADER_SIZE (sizeof(Object))

#define get_array_element_ptr(ARR_,ESIZE_,IDX_) ((byte *) (ARR_) + (IDX_) * (ESIZE_) + HEADER_SIZE)
#define get_array_word(ARR_,ESIZE_,IDX_,WPT_)        (make_word (get_array_element_ptr(ARR_,ESIZE_,IDX_), ESIZE_, WPT_))
#define set_array_word(ARR_,ESIZE_,IDX_,WRD_)   (save_word (get_array_element_ptr(ARR_,ESIZE_,IDX_), ESIZE_, WRD_))

#define array_start(OBJ_)   ((byte *) (OBJ_) + HEADER_SIZE)
#define jbyte_array(OBJ_)   ((JBYTE *) array_start(OBJ_))
#define ref_array(OBJ_)     ((REFERENCE *) array_start(OBJ_))
#define jint_array(OBJ_)    ((JINT *) array_start(OBJ_))
#define jshort_array(OBJ_)  ((JSHORT *) array_start(OBJ_))
#define jlong_array(OBJ_)   ((JLONG *) array_start(OBJ_))
#define jfloat_array(OBJ_)  ((JFLOAT *) array_start(OBJ_))

#endif _MEMORY_H


