
#ifndef _CLASSES_H
#define _CLASSES_H

#include "types.h"
#include "language.h"

#define THREAD_MASK    0x003F
#define THREAD_SHIFT   0x0002
#define CLASS_MASK     0x00FF
#define ELEM_SIZE_MASK 0x00FF
#define ARRAY_MASK     0x4000

#define is_array(OBJ_)         ((OBJ_)->flags & ARRAY_MASK)
#define get_class_index(OBJ_)  ((OBJ_)->flags & CLASS_MASK)
#define get_element_size(ARR_) ((ARR_)->flags & ELEM_SIZE_MASK)

// Double-check these data structures
// with the Java declaration of each.

typedef struct S_Object
{
  /**
   * Object flags.
   * bit 15: reserved for GC.
   * bit 14: is primitive array
   * Primitive arrays:
   *   bits 0-7: Element size.
   * Other:
   *   bits 0-7: Class index.
   */
  TWOBYTES flags;

  /**
   * Synchronization state.
   * bit 0-7: monitor count.
   * bit 8-15: Thread index.
   */
  TWOBYTES syncInfo;

} Object;

typedef struct S_Thread
{
  //--------------- Object fields first:
  TWOBYTES flags;
  TWOBYTES syncInfo;

  //--------------- Thread fields:
  REFERENCE nextThread;
  REFERENCE waitingOn;
  REFERENCE stackFrameArray;
  REFERENCE stackArray;
  REFERENCE currentStackFrame;
  byte threadId;
  byte state;
} Thread;

inline byte get_thread_id (Object *obj)
{
  return (byte) ((obj->syncInfo >> THREAD_SHIFT) & THREAD_MASK);
}

inline void set_class (Object *obj, byte classIndex)
{
  obj->flags = classIndex;
}

#endif _CLASSES_H









