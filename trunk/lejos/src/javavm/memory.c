
#include "types.h"
#include "trace.h"
#include "constants.h"
#include "specialsignatures.h"
#include "memory.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"

#ifdef VERIFY
static boolean memoryInitialized = false;
#endif

#ifdef EMULATE

extern byte *gMemory;
extern TWOBYTES gMemorySize;

#define USER_MEMORY_START gMemory
#define USER_MEMORY_TOP   (gMemory + gMemorySize)

#else // not EMULATE

extern char _end;

#define USER_MEMORY_START ((TWOBYTES *) &_end)
#define USER_MEMORY_TOP ((TWOBYTES *) 0xFFFE)

#endif EMULATE

#define MAX_ALLOC_PTR (USER_MEMORY_TOP - NATIVE_STACK_SIZE)

#define NULL_OFFSET 0xFFFF

#define HEADER_SIZE (sizeof(Object))
// Size of object header in 2-byte words
#define NORM_OBJ_SIZE ((HEADER_SIZE + 1) / 2)
// Size of stack frame in 2-byte words
#define NORM_SF_SIZE ((sizeof(StackFrame) + 1) / 2)

byte typeSize[] = { 
  4, // 0 == T_REFERENCE
  NORM_SF_SIZE * 2, // 1 == T_STACKFRAME
  0, // 2
  0, // 3
  1, // 4 == T_BOOLEAN
  2, // 5 == T_CHAR
  4, // 6 == T_FLOAT
  8, // 7 == T_DOUBLE
  1, // 8 == T_BYTE
  2, // 9 == T_SHORT
  4, // 10 == T_INT
  8  // 11 == T_LONG
};

// Two-byte pointers are used so that pointer
// arithmetic works as expected.

static TWOBYTES freeOffset = NULL_OFFSET;
static TWOBYTES *startPtr;

inline void set_class (Object *obj, byte classIndex)
{
  obj->flags = classIndex;
}

inline void set_array (Object *obj, byte elemSize, byte length)
{
  obj->flags = ARRAY_MASK | ((elemSize - 1) << ELEM_SIZE_SHIFT) | length;
}

/**
 * Allocates and initializes the state of
 * an object, which is pushed on the operand
 * stack.
 */
Object *new_object_for_class (byte classIndex)
{
  Object *ref;
  // TBD: Check for class initialization!
  ref = (Object *) checked_alloc (get_class_record(classIndex)->size);
  if (ref == null)
    return null;

  // Initialize default values
  set_class (ref, classIndex);
  initialize_state (ref, get_class_record(classIndex)->classSize);
  return ref;
}

/**
 * @param numWords Number of 2-byte words used in allocating the object.
 */
TWOBYTES initialize_state (Object *objRef, TWOBYTES numWords)
{
  TWOBYTES *ptr;

  numWords -= NORM_OBJ_SIZE;
  ptr = ((TWOBYTES *) objRef) + NORM_OBJ_SIZE;
  for (i = numWords; i-- > 0;)
  {
    *ptr++ = 0x0000;
  }  
}

TWOBYTES get_array_size (byte length, byte elemSize)
{
  return NORM_OBJ_SIZE + ((TWOBYTES) length * elemSize) / 2;
}

/**
 * Allocates an array. The size of the array is NORM_OBJ_SIZE
 * plus the size necessary to allocate <code>length</code> elements
 * of the given type.
 */
Object *new_primitive_array (byte primitiveType, STACKWORD length)
{
  Object *ref;
  byte elemSize;

  // Hack to disallow allocations longer than 255:
  if (length > 0xFF)
  {
    throw_excetion (outOfMemoryError);
    return null;
  }
  elemSize = typeSize[primitiveType];
  allocSize = get_array_size ((byte) length, elemSize);
  ref = checked_alloc (allocSize);
  if (ref == null)
    return null;
  set_array (ref, elemSize, (byte) length);
  initialize_state (ref, allocSize);
  return ref;
}

void free_array (Object *objectRef)
{
  #ifdef VERIFY
  assert (is_array(objectRef), MEMORY7);
  #endif VERIFY

  deallocate ((TWOBYTES *) objectRef, get_array_size (
              get_array_length (objectRef),
              get_element_size (objectRef)));  
}

/**
 * @param elemType Type of primitive element of multi-dimensional array.
 * @param totalDimensions Same as number of brackets in array class descriptor.
 * @param reqDimensions Number of requested dimensions for allocation.
 */
Object *new_multi_array (byte elemType, byte totalDimensions, 
                         byte reqDimensions)
{
  // TBD: change instruction code

  STACKWORD numElements;
  Object *ref;

  #ifdef VERIFY
  assert (totalDimensions >= 1, MEMORY6);
  assert (reqDimensions <= totalDimensions, MEMORY8);
  #endif

  if (reqDimensions == 0)
    return null;
  numElements = *stackTop--;
  if (totalDimensions == 1)
    return new_primitive_array (elemType, numElements);
  ref = new_primitive_array (T_REFERENCE, numElements);
  if (ref == null)
    return null;
  while (--numElements >= 0)
  {
    set_array_element (ref, 4, numElements, 
      new_multi_array (elemType, totalDimensions - 1, reqDimensions - 1));
  }
  return ref;
}

STACKWORD get_array_element (byte *aRef, byte aSize, TWOBYTES aIndex)
{
  return make_word (aRef + aIndex * aSize + HEADER_SIZE, aSize); 
}

void set_array_element (byte *aRef, byte aSize, TWOBYTES aIndex, 
                        STACKWORD aWord)
{
  copy_word (pRef + aIndex * aSize + HEADER_SIZE, aSize, aWord);
}

// TBD: Constants and statics should be written
// in this manner.

STACKWORD make_word (byte *ptr, byte aSize)
{
  STACKWORD result = 0;
  byte i;

  for (i = 0; i < aSize; i++)
  {
    result = result << 8;
    result |= *ptr++;
  }
  return result;
}

void copy_word (byte *ptr, byte aSize, STACKWORD aWord)
{
  byte i;

  for (i = aSize; --i >= 0;)
  {
    ptr[i] = (byte) (aWord & 0xFF);
    aWord = aWord >> 8;
  }
}

TWOBYTES *checked_alloc (TWOBYTES size)
{
  TWOBYTES *ref;
  ref = allocate (get_class_record(classIndex)->size);
  if (ref == null)
  {
    #ifdef VERIFY
    assert (outOfMemoryError != null, MEMORY5);
    #endif
    throw_exception (outOfMemoryError);
    return null;
  }
  ref->syncInfo = 0;
  #ifdef SAFE
  ref->flags = 0;
  #endif
  return ref;
}

// Notes on allocation:
// 1. It's first-fit.
// 2. First 2 bytes of free block is size.
// 3. Second 2 bytes of free block is abs. offset of next free block.

/**
 * @param offset Size of classes and other
 *               loaded structures, in 2-byte words.
 */
void init_memory (TWOBYTES offset)
{
  #ifdef VERIFY
  memoryInitialized = true;
  #endif

  startPtr = USER_MEMORY_START + offset;
  deallocate (startPtr, MAX_ALLOC_PTR - startPtr);
}

/**
 * @param size Size of object in 2-byte words.
 */
TWOBYTES *allocate (TWOBYTES size)
{
  register TWOBYTES *ptr;
  TWOBYTES *anchorOffsetRef;

  anchorOffsetRef = &freeOffset;
  while (*anchorOffsetRef != NULL_OFFSET)
  { 
    ptr = startPtr + *anchorOffsetRef;
    if (ptr[0] >= size + 2)
    {
      ptr[0] = ptr[0] - size;
      return ptr + ptr[0];
    }
    if (ptr[0] >= size)
    {
      *anchorOffsetRef = ptr[1]; 
      return ptr;     
    }
    anchorOffsetRef = &(ptr[1]);
  }
  return null;      
}

void deallocate (TWOBYTES *ptr, TWOBYTES size)
{
  // TBD: consolidate free blocks
  ptr[0] = size;
  ptr[1] = freeOffset;
  freeOffset = ptr - startPtr;
}







