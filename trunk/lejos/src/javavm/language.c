/**
 * The language datastructures.
 */

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
#include "native.h"

#define F_SIZE_MASK    0xE0
#define F_SIZE_SHIFT   5
#define F_OFFSET_MASK  0x1F

#ifdef VERIFY
boolean classesInitialized = false;
#endif

#define get_stack_object(MREC_)  ((Object *) *(stackTop - (MREC_)->numParameters + 1))

// Reliable globals:

void *installedBinary;

// Temporary globals:

// (Gotta be careful with these; a lot of stuff
// is not reentrant because of globals like these).

static MethodRecord *gMethodRecord;
static ClassRecord *gClassRecord;
static byte gByte2;

// Methods:

byte get_class_index (Object *obj)
{
  byte f;

  f = obj->flags;
  if (f & ARRAY_MASK)
    return JAVA_LANG_OBJECT;
  return (f & CLASS_MASK);
}

/**
 * Puts or gets field.
 */
void handle_field (byte hiByte, byte loByte, boolean doPut, boolean aStatic,
                   byte *btAddr)
{
  byte *fieldBase;
  STATICFIELD fieldRecord;
  byte fieldSize;
  byte numWordsMinus1;

  #if DEBUG_FIELDS
  printf ("handleField: %d, %d, %d, %d\n", (int) hiByte, (int) loByte, 
          (int) doPut, (int) aStatic);
  #endif

  if (aStatic)
  {
    if (dispatch_static_initializer (get_class_record (hiByte), btAddr))
      return;
    fieldRecord = ((STATICFIELD *) get_static_fields_base())[loByte];
    fieldSize = ((fieldRecord >> 12) & 0x03) + 1;
    numWordsMinus1 = fieldRecord >> 14;
    fieldBase = get_static_state_base() + get_static_field_offset (fieldRecord);
  }
  else
  {
    fieldSize = ((hiByte >> F_SIZE_SHIFT) & 0x03) + 1;
    numWordsMinus1 = hiByte >> 7;
    if (doPut)
      stackTop -= (numWordsMinus1 + 1);
    #if DEBUG_FIELDS
    printf ("-- numWords-1  = %d\n", (int) numWordsMinus1);
    printf ("-- stackTop[0,1] = %d, %d\n", (int) stackTop[0], (int) stackTop[1]);
    #endif
    if (stackTop[0] == JNULL)
    {
      throw_exception (nullPointerException);
      return;
    }
    fieldBase = ((byte *) word2ptr (stackTop[0])) + 
                (((TWOBYTES) (hiByte & F_OFFSET_MASK) << 8) | loByte);
    if (doPut)
      stackTop++;
  }

  #if DEBUG_FIELDS
  printf ("-- fieldSize  = %d\n", (int) fieldSize);
  printf ("-- fieldBase  = %d\n", (int) fieldBase);
  #endif

  // fieldRecord is a counter below
  fieldRecord = 0;
  while (true)
  {
    if (doPut)
      save_word (fieldBase, fieldSize, *stackTop);
    else
      make_word (fieldBase, fieldSize, stackTop);
    if (fieldRecord++ >= numWordsMinus1)
      break;
    fieldBase += fieldSize;
    stackTop++;
  }
  if (doPut)
    stackTop -= (numWordsMinus1 + 1);
}

/**
 * @return Method index or -1.
 */
MethodRecord *find_method (ClassRecord *classRecord, TWOBYTES methodSignature)
{
  gByte = classRecord->numMethods;
  while (gByte--)
  {
    gMethodRecord = get_method_record (classRecord, gByte);
    if (gMethodRecord->signatureId == methodSignature)
      return gMethodRecord;
  }
  return null;
}

boolean dispatch_static_initializer (ClassRecord *aRec, byte *retAddr)
{
  if (is_initialized (aRec))
    return false;
  set_initialized (aRec);
  if (!has_clinit (aRec))
    return false;
  #if DEBUG_METHODS
  printf ("dispatch_static_initializer: has clinit: %d, %d\n",
          (int) aRec, (int) retAddr);
  #endif
  dispatch_special (aRec, find_method (aRec, _CLINIT__V), retAddr);
  return true;
}

void dispatch_virtual (Object *ref, TWOBYTES signature, byte *retAddr)
{
  if (ref == JNULL)
  {
    throw_exception (nullPointerException);
    return;
  }
  gByte2 = get_class_index(ref);
 LABEL_METHODLOOKUP:
  gClassRecord = get_class_record (gByte2);
  gMethodRecord = find_method (gClassRecord, signature);
  if (gMethodRecord == null)
  {
    #if SAFE
    if (gByte2 == JAVA_LANG_OBJECT)
    {
      throw_exception (noSuchMethodError);
      return;
    }
    #endif
    gByte2 = gClassRecord->parentClass;
    goto LABEL_METHODLOOKUP;
  }
  if (dispatch_special (gClassRecord, gMethodRecord, retAddr))
  {
    if (is_synchronized(gMethodRecord))
    {
      current_stackframe()->monitor = ref;
      enter_monitor (ref);
    }
  }
}

/**
 * Calls static initializer if necessary before
 * dispatching with dispatch_special().
 * @param retAddr Return bytecode address.
 * @param btAddr Backtrack bytecode address (in case
 *               static initializer is executed).
 */
void dispatch_special_checked (byte classIndex, byte methodIndex,
                               byte *retAddr, byte *btAddr)
{
  ClassRecord *classRecord;

  #if DEBUG_METHODS
  printf ("dispatch_special_checked: %d, %d, %d, %d\n",
          classIndex, methodIndex, (int) retAddr, (int) btAddr);
  #endif

  classRecord = get_class_record (classIndex);
  if (dispatch_static_initializer (classRecord, btAddr))
    return;
  dispatch_special (classRecord, get_method_record (classRecord, methodIndex),
                    retAddr);
}

/**
 * @return true iff the method was dispatched; false
 *         if an exception was thrown.
 */
boolean dispatch_special (ClassRecord *classRecord, MethodRecord *methodRecord, 
                          byte *retAddr)
{
  #if DEBUG_METHODS
  int debug_ctr;
  #endif

  STACKWORD *paramBase;
  StackFrame *stackFrame;
  byte newStackFrameIndex;

  #if DEBUG_BYTECODE
  printf ("\n------ dispatch special --------------------\n\n");
  #endif
  #if DEBUG_METHODS
  printf ("dispatch_special: %d, %d, %d\n", 
          (int) classRecord, (int) methodRecord, (int) retAddr);
  printf ("-- signature id = %d\n", methodRecord->signatureId);
  printf ("-- code offset  = %d\n", methodRecord->codeOffset);
  printf ("-- flags        = %d\n", methodRecord->mflags);
  printf ("-- num params   = %d\n", methodRecord->numParameters);
  printf ("-- localsBase   = %d\n", (int) localsBase);
  printf ("-- stackTop     = %d\n", (int) stackTop);
  #endif

  stackTop -= methodRecord->numParameters;
  paramBase = stackTop + 1;
 
  newStackFrameIndex = currentThread->stackFrameArraySize;
  if (newStackFrameIndex >= MAX_STACK_FRAMES)
  {
    throw_exception (stackOverflowError);
    return false;
  }
  if (newStackFrameIndex == 0)
  {
    stackFrame = stackframe_array();
    stackFrame->localsBase = stack_array();
  }
  else
  {
    #if DEBUG_METHODS
    for (debug_ctr = 0; debug_ctr < methodRecord->numParameters; debug_ctr++)
      printf ("-- param[%d]    = %ld\n", debug_ctr, (long) paramBase[debug_ctr]);  
    #endif

    // Save stackFrame state
    stackFrame = stackframe_array() + (newStackFrameIndex - 1);
    stackFrame->stackTop = stackTop;
    stackFrame->pc = retAddr;
    stackFrame++;
    stackFrame->localsBase = paramBase;
  }
  // Increment size of stack frame array
  currentThread->stackFrameArraySize++;
  // Initialize rest of new stack frame
  stackFrame->methodRecord = methodRecord;
  // TBD: assigning to stackFrame->pc may not be necessary
  stackFrame->pc = get_code_ptr(methodRecord);
  stackFrame->stackTop = stackFrame->localsBase + methodRecord->numLocals - 1;
  stackFrame->monitor = null;
  // Initialize auxiliary globals
  pc = stackFrame->pc;
  localsBase = stackFrame->localsBase;
  stackTop = stackFrame->stackTop;
  // Check for stack overflow
  if ((stackTop + methodRecord->maxOperands) >= (stack_array() + STACK_SIZE))
  {
    throw_exception (stackOverflowError);
    return false;
  } 
  if (is_native (methodRecord))
  {
    dispatch_native (methodRecord->signatureId, paramBase);
    return true;
  }
  return true;
}

/**
 */
void do_return (byte numWords)
{
  StackFrame *stackFrame;
  STACKWORD *sourcePtr;

  // Place source ptr below data to be copied up the stack
  sourcePtr = stackTop - numWords;
  stackFrame = current_stackframe();

  #ifdef DEBUG_METHODS
  printf ("do_return: method: %d  #  num. words: %d\n", 
          stackFrame->methodRecord->signatureId, numWords);
  #endif

  #ifdef VERIFY
  assert (stackFrame != null, LANGUAGE3);
  #endif
  if (stackFrame->monitor != null)
    exit_monitor (stackFrame->monitor);

  #if DEBUG_THREADS
  printf ("do_return: stack frame array size: %d\n", currentThread->stackFrameArraySize);
  #endif

  if (currentThread->stackFrameArraySize == 1)
  {
    #if DEBUG_METHODS
    printf ("do_return: thread is done: %d\n", (int) currentThread);
    #endif
    currentThread->state = DEAD;
    switch_thread();
    return;
  }
  currentThread->stackFrameArraySize--;
  stackFrame--;
  pc = stackFrame->pc;
  stackTop = stackFrame->stackTop;
  localsBase = stackFrame->localsBase;

  #if DEBUG_METHODS
  printf ("do_return: stack reset to:\n");
  printf ("-- localsBase   = %d\n", (int) localsBase);
  printf ("-- stackTop     = %d\n", (int) stackTop);
  #endif

  while (numWords--)
  {
    *(++stackTop) = *(++sourcePtr);
  }  
}

/**
 * @return 1 or 0.
 */
STACKWORD instance_of (Object *obj, byte classIndex)
{
  byte rtType;

  if (obj == null)
    return 0;
  rtType = get_class_index(obj);
  // TBD: support for interfaces
  if (is_interface (get_class_record(classIndex)))
    return 1;
 LABEL_INSTANCE:
  if (rtType == classIndex)
    return 1;
  if (rtType == JAVA_LANG_OBJECT)
    return 0;
  rtType = get_class_record(rtType)->parentClass;
  goto LABEL_INSTANCE;
}





