
#include "types.h"

#ifndef _CLASSES_H
#define _CLASSES_H

#define THREAD_SHIFT    0x0008
#define THREAD_MASK     0xFF00
#define COUNT_MASK      0x00FF
#define CLASS_MASK      0x00FF
#define ELEM_SIZE_MASK  0x003F
#define ELEM_SIZE_SHIFT 0x0008
#define ARRAY_MASK      0x4000
#define LENGTH_MASK     0x00FF

#define is_array(OBJ_)          (((OBJ_)->flags & ARRAY_MASK) != 0)
#define get_element_size(ARR_)  ((((ARR_)->flags >> ELEM_SIZE_SHIFT) & ELEM_SIZE_MASK) + 1)
#define get_array_length(ARR_)  ((ARR_)->flags & LENGTH_MASK)
#define get_monitor_count(OBJ_) ((OBJ_)->syncInfo & COUNT_MASK)

// Double-check these data structures
// with the Java declaration of each.

typedef struct S_Object
{
  /**
   * Object flags.
   * bit 15: reserved for GC.
   * bit 14: is primitive array
   * Primitive arrays:
   *   bits 0-7: Array length.
   *   bits 8-13: Element size - 1.
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
  Object _super;

  REFERENCE nextThread;
  REFERENCE waitingOn;
  REFERENCE stackFrameArray;
  REFERENCE stackArray;
  REFERENCE isReferenceArray;
  JBYTE stackFrameArraySize;
  JBYTE threadId;
  JBYTE state;
} Thread;

typedef struct S_String
{
  Object _super;

  REFERENCE characters;
} String;

#endif _CLASSES_H









