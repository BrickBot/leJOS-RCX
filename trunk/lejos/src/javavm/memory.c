
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
REFERENCE new_object_for_class (byte classIndex)
{
  REFERENCE ref;
  // TBD: Check for class initialization!
  ref = (REFERENCE) object_alloc (get_class_record(classIndex)->size);
  if (ref == null)
  {
    #ifdef VERIFY
    assert (outOfMemoryError != null, MEMORY5);
    #endif
    throw_exception (outOfMemoryError);
    return;
  }
  // Initialize default values
  set_class (ref, classIndex);
  initialize_object (ref);
  return ref;
}

/**
 * Allocates an array. The size of the array is NORM_OBJ_SIZE
 * plus the size necessary to allocate <code>length</code> elements
 * of the given type.
 */
REFERENCE new_primitive_array (byte primitiveType, STACKWORD length)
{
  REFERENCE ref;
  ref = object_alloc (NORM_OBJ_SIZE + (length * typeSize[primitiveType]) / 2);
  set_class (ref, ARRAY_CLASS);
  // TBD: initialize, set length
  return ref;
}

REFERENCE new_multi_array (byte classIndex, STACKWORD dimensions)
{
  byte nextClass;
  STACKWORD numElements;
  REFERENCE ref;
  TWOBYTES i;  

  // TBD: review validity
  if (dimensions == 0)
    return null;

  #ifdef VERIFY
  assert (is_array (get_class_record(classIndex)), MEMORY6);
  #endif

  ref = new_primitive_array (T_REFERENCE, *stackTop);
  nextClass = get_class_record(classIndex)->arrayElementType;
  numElements = *stackTop--;
  for (i = 0; i < numElements; i++)
  {
    set_array_element (ref, 4, i, new_multi_array (nextClass, dimensions - 1));
  }
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

// TBD: new allocation methods

// Notes on allocation:
// 1. It's first-fit.
// 2. First 2 bytes of free block is size.
// 3. Second 2 bytes of free block is abs. offset of next free block.

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







