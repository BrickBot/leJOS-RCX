
#include "types.h"
#include "trace.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "memory.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "exceptions.h"

#ifdef VERIFY
static boolean memoryInitialized = false;
#endif

#ifdef EMULATE

extern TWOBYTES *gMemory;
extern TWOBYTES  gMemorySize;

#define USER_MEMORY_START gMemory
#define TOTAL_MEMORY_SIZE gMemorySize

#else // not EMULATE

extern char _end;

#define USER_MEMORY_START ((TWOBYTES *) &_end)
#define TOTAL_MEMORY_SIZE  (0xFFFE - ((TWOBYTES) &_end))

#endif EMULATE

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

extern Object *memcheck_allocate (TWOBYTES size);
extern void initialize_state (Object *objRef, TWOBYTES numWords);
extern void deallocate (TWOBYTES *ptr, TWOBYTES size);
extern TWOBYTES *allocate (TWOBYTES size);
extern void set_array_element (byte *aRef, byte aSize, TWOBYTES aIndex, STACKWORD aWord);
extern STACKWORD make_word (byte *ptr, byte aSize);
extern void copy_word (byte *ptr, byte aSize, STACKWORD aWord);

inline void set_class (Object *obj, byte classIndex)
{
  obj->flags = classIndex;
}

inline void set_array (Object *obj, byte elemSize, byte length)
{
  obj->flags = ARRAY_MASK | ((elemSize - 1) << ELEM_SIZE_SHIFT) | length;
}

/**
 * Checks if the class needs to be initialized.
 * If so, the static initializer is dispatched.
 * Otherwise, an instance of the class is allocated.
 *
 * @param btAddr Back-track PC address, in case
 *               a static initializer needs to be invoked.
 * @return Object reference or <code>null</code> iff
 *         NullPointerException had to be thrown or
 *         static initializer had to be invoked.
 */
Object *new_object_checked (byte classIndex, byte *btAddr)
{
  if (dispatch_static_initializer (classIndex, btAddr))
    return null;
  return new_object_for_class (classIndex);
}

/**
 * Allocates and initializes the state of
 * an object, which is pushed on the operand
 * stack.
 */
Object *new_object_for_class (byte classIndex)
{
  Object *ref;
  TWOBYTES instanceSize;

  // TBD: Check for class initialization!
  instanceSize = get_class_record(classIndex)->classSize;
  ref = memcheck_allocate (instanceSize);
  if (ref == null)
    return null;

  // Initialize default values
  set_class (ref, classIndex);
  initialize_state (ref, instanceSize);
  return ref;
}

/**
 * @param numWords Number of 2-byte words used in allocating the object.
 */
void initialize_state (Object *objRef, TWOBYTES numWords)
{
  register TWOBYTES *ptr;

  numWords -= NORM_OBJ_SIZE;
  ptr = ((TWOBYTES *) objRef) + NORM_OBJ_SIZE;
  while (numWords-- > 0)
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
  TWOBYTES allocSize;

  // Hack to disallow allocations longer than 255:
  if (length > 0xFF)
  {
    throw_exception (outOfMemoryError);
    return null;
  }
  elemSize = typeSize[primitiveType];
  allocSize = get_array_size ((byte) length, elemSize);
  ref = memcheck_allocate (allocSize);
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
    set_array_element ((byte *) ref, 4, numElements, 
      (STACKWORD) new_multi_array (elemType, 
         totalDimensions - 1, reqDimensions - 1));
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
  copy_word (aRef + aIndex * aSize + HEADER_SIZE, aSize, aWord);
}

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

  for (i = aSize; i-- > 0;)
  {
    ptr[i] = (byte) (aWord & 0xFF);
    aWord = aWord >> 8;
  }
}

Object *memcheck_allocate (TWOBYTES size)
{
  Object *ref;
  ref = (Object *) allocate (size);
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
  deallocate (startPtr, TOTAL_MEMORY_SIZE - offset);
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







