
#include "assert.h"
#include "classes.h"
#include "types.h"
#include "exceptions.h"

#ifdef VERIFY
static boolean memoryInitialized = false;
#endif

#define USER_MEMORY_START ((TWOBYTES *) &_end)
#define USER_MEMORY_TOP ((TWOBYTES *) 0xFFFE)
#define MAX_ALLOC_PTR (USER_MEMORY_TOP - NATIVE_STACK_SIZE)
#define MAX_OBJECT_SIZE 1023

#define NULL_OFFSET 0xFFFF

// Bit 0: (0 == free, 1 == allocated)
#define FREE_BLOCK_MASK 0x0001
// Shift flags to get size of free block
#define FREE_SIZE_SHIFT 0x0001
// Mask of flag after shift
#define FREE_SIZE_MASK 0x7FFF
// Free block of zero size
#define FREE_BLOCK_0 0x0000

// Size of object header in 2-byte words
#define NORM_OBJ_SIZE ((sizeof(Object) + 1) / 2)
// Size of stack frame in 2-byte words
#define NORM_SF_SIZE ((sizeof(StackFrame) + 1) / 2)

/**
 * This is at the end of this firmware (I hope)
 */
extern char _end;

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
 * Allocates and initializes the state of
 * an object, which is pushed on the operand
 * stack.
 */
Object *new_object_for_class (byte classIndex)
{
  Object *ref;
  // TBD: Check for class initialization!
  ref = (Object *) allocate (get_class_record(classIndex)->size);
  if (ref == null)
    return null;

  // Initialize default values
  set_class (ref, classIndex);
  initialize_object (ref);
  return ref;
}

inline TWOBYTES get_array_size (byte length, byte elemSize)
{
  return NORM_OBJ_SIZE + ((byte) length * elemSize) / 2;
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
  ref = checked_alloc (get_array_size ((byte) length, elemSize));
  if (ref == null)
    return null;
  set_array (ref, elemSize, (byte) length);
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

Object *new_multi_array (byte classIndex, STACKWORD dimensions)
{
  byte nextClass;
  STACKWORD numElements;
  Object *ref;
  TWOBYTES i;  

  // TBD: review validity
  if (dimensions == 0)
    return null;

  #ifdef VERIFY
  assert (is_array (get_class_record(classIndex)), MEMORY6);
  #endif

  ref = new_primitive_array (T_REFERENCE, *stackTop);
  if (ref == null)
    return null;
  nextClass = get_class_record(classIndex)->arrayElementType;
  numElements = *stackTop--;
  for (i = 0; i < numElements; i++)
  {
    set_array_element (ref, 4, i, new_multi_array (nextClass, dimensions - 1));
  }
  return ref;
}

STACKWORD get_array_element (byte *aRef, byte aSize, TWOBYTES aIndex)
{
  return make_word (aRef + aIndex * aSize + ARRAYHEADERSIZE, aSize); 
}

void set_array_element (byte *aRef, byte aSize, TWOBYTES aIndex, 
                        STACKWORD aWord)
{
  copy_word (pRef + aIndex * aSize + ARRAYHEADERSIZE, aSize, aWord);
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







