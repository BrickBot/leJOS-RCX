
#include "types.h"

#ifndef _CLASSES_H
#define _CLASSES_H

#define THREAD_SHIFT    0x0008
#define THREAD_MASK     0xFF00
#define COUNT_MASK      0x00FF
#define CLASS_MASK      0x00FF
#define ELEM_SIZE_MASK  0x0007
#define ELEM_SIZE_SHIFT 0x0008
#define ARRAY_MASK      0x4000
#define LENGTH_MASK     0x00FF

#define is_array(OBJ_)          ((OBJ_)->flags & ARRAY_MASK)
#define get_class_index(OBJ_)   ((OBJ_)->flags & CLASS_MASK)
#define get_element_size(ARR_)  ((((ARR_)->flags >> ELEM_SIZE_SHIFT) & ELEM_SIZE_MASK) + 1)
#define get_array_length(ARR_)  ((ARR_)->flags & LENGTH_MASK)
#define get_monitor_count(OBJ_) ((OBJ_)->flags & COUNT_MASK)

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
   *   bits 8-10: Element size - 1.
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

#endif _CLASSES_H









