
#ifndef _CLASSES_H
#define _CLASSES_H

#include "types.h"
#include "language.h"

#define GC_MASK 0x0001
#define GC_SHIFT 0x0001
#define THREAD_MASK 0x003F
#define THREAD_SHIFT 0x0002
#define CLASS_MASK 0x00FF
#define CLASS_SHIFT 0x0008
#define ALLOCATED_BLOCK_0 0x0001

#define mk_pobject(WORD_) ((Object *)(WORD_))

// Double-check these data structures
// with the Java declaration of each.

typedef struct S_Object
{
  /**
   * The flags field is the only one not declared
   * or initialized:
   *
   * bit 0: (0 == free block, 1 == allocated block)
   * if (free block)
   * {
   *   bit 1-15: size in 2-byte words.
   * }
   * else
   * {
   *   bit 1: Garbage collection mark.
   *   bit 8-15: Class index.
   * }
   */
  TWOBYTES flags;

  /**
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

inline byte get_class_index (Object *obj)
{
  return (byte) ((obj->flags >> CLASS_SHIFT) & CLASS_MASK);
}

inline TWOBYTES get_size (Object *obj)
{
  return get_class_record(get_class_index(obj))->classSize;
}

inline void set_class (Object *obj, byte classIndex)
{
  obj->flags = ALLOCATED_BLOCK_0 | ((TWOBYTES) classIndex << CLASS_SHIFT);
}

#endif _CLASSES_H









