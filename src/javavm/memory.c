
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

#define NULL_OFFSET 0xFFFF

// Size of stack frame in 2-byte words
#define NORM_SF_SIZE ((sizeof(StackFrame) + 1) / 2)

byte typeSize[] = { 
  4, // 0 == T_REFERENCE
  SF_SIZE, // 1 == T_STACKFRAME
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

/**
 * Beginning of heap.
 */
static TWOBYTES *startPtr;
static TWOBYTES *memoryTop;
static TWOBYTES *allocPtr;
static TWOBYTES free;	// Total number of free words in heap

extern void deallocate (TWOBYTES *ptr, TWOBYTES size);
extern TWOBYTES *allocate (TWOBYTES size);

/**
 * @param numWords Number of 2-byte words used in allocating the object.
 */
#define initialize_state(OBJ_,NWORDS_) zero_mem(((TWOBYTES *) (OBJ_)) + NORM_OBJ_SIZE, (NWORDS_) - NORM_OBJ_SIZE)
#define get_object_size(OBJ_)          (get_class_record(get_na_class_index(OBJ_))->classSize)

/**
 * Zeroes out memory.
 * @param ptr The starting address.
 * @param numWords Number of two-byte words to clear.
 */
void zero_mem (register TWOBYTES *ptr, register TWOBYTES numWords)
{
  while (numWords--)
    *ptr++ = 0;
}

static inline void set_array (Object *obj, const byte elemType, const TWOBYTES length)
{
  #ifdef VERIFY
  assert (elemType <= (ELEM_TYPE_MASK >> ELEM_TYPE_SHIFT), MEMORY0); 
  assert (length <= (ARRAY_LENGTH_MASK >> ARRAY_LENGTH_SHIFT), MEMORY1);
  #endif
  obj->flags = IS_ALLOCATED_MASK | IS_ARRAY_MASK | ((TWOBYTES) elemType << ELEM_TYPE_SHIFT) | length;
  #ifdef VERIFY
  assert (is_array(obj), MEMORY3);
  #endif
}

inline Object *memcheck_allocate (const TWOBYTES size)
{
  Object *ref;
  ref = (Object *) allocate (size);
  if (ref == JNULL)
  {
    #ifdef VERIFY
    assert (outOfMemoryError != null, MEMORY5);
    #endif
    throw_exception (outOfMemoryError);
    return JNULL;
  }
  ref->syncInfo = 0;
  #ifdef SAFE
  ref->flags = 0;
  #endif
  return ref;
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
Object *new_object_checked (const byte classIndex, byte *btAddr)
{
  #if 0
  trace (-1, classIndex, 0);
  #endif
  if (dispatch_static_initializer (get_class_record(classIndex), btAddr))
  {
#if DEBUG_MEMORY
  printf("New object checked returning null\n");
#endif
    return JNULL;
  }
  return new_object_for_class (classIndex);
}

/**
 * Allocates and initializes the state of
 * an object. It does not dispatch static
 * initializers.
 */
Object *new_object_for_class (const byte classIndex)
{
  Object *ref;
  TWOBYTES instanceSize;

#if DEBUG_MEMORY
  printf("New object for class\n");
#endif
  instanceSize = get_class_record(classIndex)->classSize;
  ref = memcheck_allocate (instanceSize);
  if (ref == null)
  {
#if DEBUG_MEMORY
  printf("New object for class returning null\n");
#endif
    return JNULL;
  }

  // Initialize default values

  ref->flags = IS_ALLOCATED_MASK | classIndex;
  initialize_state (ref, instanceSize);

  #if DEBUG_OBJECTS || DEBUG_MEMORY
  printf ("new_object_for_class: returning %d\n", (int) ref);
  #endif

  return ref;
}

/**
 * Return the size in words of an array of the given type
 */
TWOBYTES comp_array_size (const TWOBYTES length, const byte elemType)
{
  return NORM_OBJ_SIZE + (((TWOBYTES) length * typeSize[elemType]) + 1) / 2;
}

/**
 * Allocates an array. The size of the array is NORM_OBJ_SIZE
 * plus the size necessary to contain <code>length</code> elements
 * of the given type.
 */
Object *new_primitive_array (const byte primitiveType, STACKWORD length)
{
  Object *ref;
  TWOBYTES allocSize;

  // Check if length is too large to be representable
  if (length > (ARRAY_LENGTH_MASK >> ARRAY_LENGTH_SHIFT))
  {
    throw_exception (outOfMemoryError);
    return JNULL;
  }
  allocSize = comp_array_size (length, primitiveType);
  ref = memcheck_allocate (allocSize);
  if (ref == null)
    return JNULL;
  set_array (ref, primitiveType, length);
  initialize_state (ref, allocSize);
  return ref;
}

TWOBYTES get_array_size (Object *obj)
{
  return comp_array_size (get_array_length (obj),
                          get_element_type (obj));	
}

void free_array (Object *objectRef)
{
  #ifdef VERIFY
  assert (is_array(objectRef), MEMORY7);
  #endif // VERIFY

  deallocate ((TWOBYTES *) objectRef, get_array_size (objectRef));
}

/**
 * @param elemType Type of primitive element of multi-dimensional array.
 * @param totalDimensions Same as number of brackets in array class descriptor.
 * @param reqDimensions Number of requested dimensions for allocation.
 * @param numElemPtr Pointer to first dimension. Next dimension at numElemPtr+1.
 */
Object *new_multi_array (byte elemType, byte totalDimensions, 
                         byte reqDimensions, STACKWORD *numElemPtr)
{
  Object *ref;

  #ifdef WIMPY_MATH
  Object *aux;
  TWOBYTES ne;
  #endif

  #ifdef VERIFY
  assert (totalDimensions >= 1, MEMORY6);
  assert (reqDimensions <= totalDimensions, MEMORY8);
  #endif

  #if 0
  printf ("new_multi_array (%d, %d, %d)\n", (int) elemType, (int) totalDimensions, (int) reqDimensions);
  #endif

  if (reqDimensions == 0)
    return JNULL;

  #if 0
  printf ("num elements: %d\n", (int) *numElemPtr);
  #endif

  if (totalDimensions == 1)
    return new_primitive_array (elemType, *numElemPtr);


  ref = new_primitive_array (T_REFERENCE, *numElemPtr);
  if (ref == JNULL)
    return JNULL;

  while ((*numElemPtr)--)
  {
    #ifdef WIMPY_MATH

    aux = new_multi_array (elemType, totalDimensions - 1, reqDimensions - 1,
      numElemPtr + 1);
    ne = *numElemPtr;
    ref_array(ref)[ne] = ptr2word (aux);

    #else

    ref_array(ref)[*numElemPtr] = ptr2word (new_multi_array (elemType, totalDimensions - 1, reqDimensions - 1, numElemPtr + 1));

    #endif // WIMPY_MATH
  }


  return ref;
}

#ifdef WIMPY_MATH

void store_word (byte *ptr, byte aSize, STACKWORD aWord)
{
  byte *wptr;
  byte ctr;

  wptr = &aWord;
  ctr = aSize;
  while (ctr--)
  {
    #if LITTLE_ENDIAN
    ptr[ctr] = wptr[aSize-ctr-1];
    #else
    ptr[ctr] = wptr[ctr];
    #endif // LITTLE_ENDIAN
  }
}

#else
/**
 * Problem here is bigendian v. littleendian. Java has its
 * words stored bigendian, intel is littleendian.
 */
STACKWORD get_word(byte *ptr, byte aSize)
{
  STACKWORD aWord = 0;
  byte ctr = 0;
  while (aSize--)
  {
    aWord = (aWord << 8) | (STACKWORD)(ptr[ctr++]);
  }
  
  return aWord;
}

void store_word (byte *ptr, byte aSize, STACKWORD aWord)
{
  while (aSize--)
  {
    ptr[aSize] = (byte) aWord;
    aWord = aWord >> 8;
  }
}

#endif // WIMPY_MATH

typedef union 
{
  struct
  {
    byte byte0;
    byte byte1;
    byte byte2;
    byte byte3;
  } st;
  STACKWORD word;
} AuxStackUnion;

void make_word (byte *ptr, byte aSize, STACKWORD *aWordPtr)
{
  // This switch statement is 
  // a workaround for a h8300-gcc bug.
  switch (aSize)
  {
    case 1:
      *aWordPtr = (JINT) (JBYTE) ptr[0];
      return;
    case 2:
      *aWordPtr = (JINT) (JSHORT) (((TWOBYTES) ptr[0] << 8) | ptr[1]);
      return;
    #ifdef VERIFY
    default:
      assert (aSize == 4, MEMORY9);
    #endif // VERIFY
  }
  #if LITTLE_ENDIAN
  ((AuxStackUnion *) aWordPtr)->st.byte0 = ptr[3];  
  ((AuxStackUnion *) aWordPtr)->st.byte1 = ptr[2];  
  ((AuxStackUnion *) aWordPtr)->st.byte2 = ptr[1];  
  ((AuxStackUnion *) aWordPtr)->st.byte3 = ptr[0];  
  #else
  ((AuxStackUnion *) aWordPtr)->st.byte0 = ptr[0];  
  ((AuxStackUnion *) aWordPtr)->st.byte1 = ptr[1];  
  ((AuxStackUnion *) aWordPtr)->st.byte2 = ptr[2];  
  ((AuxStackUnion *) aWordPtr)->st.byte3 = ptr[3];  
  #endif
}

// Notes on allocation:
// 1. It's first-fit.
// 2. First 2 bytes of free block is size.
// 3. Second 2 bytes of free block is abs. offset of next free block.

#if DEBUG_RCX_MEMORY

void scan_memory (TWOBYTES *numNodes, TWOBYTES *biggest, TWOBYTES *freeMem)
{
}

#endif // DEBUG_RCX_MEMORY

/**
 * @param ptr Beginning of heap.
 * @param size Size of heap in 2-byte words.
 */
void init_memory (void *ptr, TWOBYTES size)
{
  #ifdef VERIFY
  memoryInitialized = true;
  #endif

  startPtr = ptr;
  memoryTop = ((TWOBYTES *) ptr) + size;
  allocPtr = ptr;
  #if DEBUG_MEMORY
  printf ("Setting start of memory to %d\n", (int) startPtr);
  printf ("Going to reserve %d words\n", size);
  #endif
  free = 0;
  deallocate (ptr, size);
}

TWOBYTES *allocate (TWOBYTES size)
{
  TWOBYTES blockHeader;
  TWOBYTES *ptr;
  boolean rolled = false;
  
  ptr = allocPtr;
  for (;;)
  {
    blockHeader = ptr[0];
    if (blockHeader & IS_ALLOCATED_MASK)
    {
      TWOBYTES s = (blockHeader & IS_ARRAY_MASK) ? get_array_size ((Object *) ptr) :
                                             get_object_size ((Object *) ptr);
      ptr += s;
    }
    else
    {
      if (size <= blockHeader)
      {
        allocPtr = ptr + size;
	if (size < blockHeader)
          allocPtr[0] = blockHeader - size;
        if (allocPtr >= memoryTop)
          allocPtr = startPtr;
        free -= size;
        return ptr;
      }
      ptr += blockHeader;
    }
    if (ptr >= memoryTop)
    {
      ptr = startPtr;
      rolled = true;
    }      
    if (rolled && ptr >= allocPtr)
      return JNULL;
  }
}

/**
 * Doesn't coallesce adjacent free blocks. But then nothing gets freed
 * anyway.
 * @param size Must be exactly same size used in allocation.
 */
void deallocate (TWOBYTES *ptr, TWOBYTES size)
{
  #ifdef VERIFY
  assert (size <= (FREE_BLOCK_SIZE_MASK >> FREE_BLOCK_SIZE_SHIFT), MEMORY3);
  #endif

  free += size;  
  ptr[0] = size;
}

int getHeapSize() {
  return ((int)(memoryTop - startPtr)) << 1;
}

int getHeapFree() {
  return ((int)free) << 1;
}
