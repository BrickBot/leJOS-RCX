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

void *installedBinary;

#ifdef VERIFY
boolean classesInitialized = false;
#endif

#define get_stack_object(MREC_)  ((Object *) *(stackTop - (MREC_)->numParameters + 1))

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
  ClassRecord *classRecord;
  byte *sourcePtr;
  byte *destPtr;
  byte *fieldBase;
  STATICFIELD fieldRecord;
  TWOBYTES offset;
  byte fieldSize;
  byte numWordsMinus1;

  if (aStatic)
  {
    classRecord = get_class_record (hiByte);
    if (dispatch_static_initializer (classRecord, btAddr))
      return;
    fieldRecord = ((STATICFIELD *) get_static_fields_base())[loByte];
    fieldSize = get_static_field_size (fieldRecord);
    fieldBase = get_static_state_base() + get_static_field_offset (fieldRecord);
  }
  else
  {
    if (stackTop[0] == JNULL)
    {
      throw_exception (nullPointerException);
      return;
    }
    fieldSize = (hiByte >> F_SIZE_SHIFT) + 1;
    offset = ((TWOBYTES) (hiByte & F_OFFSET_MASK) << 8) | loByte;
    fieldBase = ((byte *) word2ptr (stackTop[0])) + offset;
    // Pop object reference
    stackTop--;
  }

  numWordsMinus1 = (fieldSize <= 4) ? 0 : 1;
  if (numWordsMinus1)
  {
    // Adjust field size for one word only 
    fieldSize = 4;
    // Backtrack stack pointer
    stackTop--;
  }  
  while (true)
  {
    if (doPut)
      save_word (fieldBase, fieldSize, *stackTop++);
    else
      make_word (fieldBase, fieldSize, ++stackTop);
    if (numWordsMinus1-- == 0)
      break;
    fieldBase += fieldSize;
  }
}

/**
 * @return Method index or -1.
 */
MethodRecord *find_method (ClassRecord *classRecord, TWOBYTES methodSignature)
{
  MethodRecord *methodRecord;
  byte i;

  for (i = 0; i < classRecord->numMethods; i++)
  {
    methodRecord = get_method_record (classRecord, i);
    if (methodRecord->signatureId == methodSignature)
      return methodRecord;
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
  dispatch_special (aRec, find_method (aRec, _CLINIT__V), retAddr);
  return true;
}

void dispatch_virtual (Object *ref, TWOBYTES signature, byte *retAddr)
{
  ClassRecord *classRecord;
  MethodRecord *methodRecord;
  byte classIndex;

  if (ref == JNULL)
  {
    throw_exception (nullPointerException);
    return;
  }
  classIndex = get_class_index(ref);
 LABEL_METHODLOOKUP:
  classRecord = get_class_record (classIndex);
  methodRecord = find_method (classRecord, signature);
  if (methodRecord == null)
  {
    if (classIndex == JAVA_LANG_OBJECT)
    {
      throw_exception (noSuchMethodError);
      return;
    }
    classIndex = classRecord->parentClass;
    goto LABEL_METHODLOOKUP;
  }
  if (dispatch_special (classRecord, methodRecord, retAddr))
  {
    if (is_synchronized(methodRecord))
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

  classRecord = get_class_record (classIndex);
  if (dispatch_static_initializer (classRecord, btAddr))
    return;
  dispatch_special (classRecord, get_method_record (classRecord, methodIndex),
                    retAddr);
}

boolean dispatch_special (ClassRecord *classRecord, MethodRecord *methodRecord, 
                          byte *retAddr)
{
  STACKWORD *paramBase;
  StackFrame *stackFrame;
  byte newStackFrameIndex;

  paramBase = stackTop - methodRecord->numParameters + 1;
  newStackFrameIndex = currentThread->stackFrameArraySize;
  if (is_native (methodRecord))
  {
    #ifdef VERIFY
    assert (newStackFrameIndex != 0, LANGUAGE0);
    #endif
    // TBD: This will only work if dispatch_native doesn't itself dispatch.
    dispatch_native (methodRecord->signatureId, paramBase);
    pc = retAddr;
    return true;
  }
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
  stackFrame->pc = get_binary_base() + methodRecord->codeOffset;
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
  #ifdef VERIFY
  assert (stackFrame != null, LANGUAGE3);
  #endif
  if (stackFrame->monitor != null)
    exit_monitor (stackFrame->monitor);
  if (currentThread->stackFrameArraySize == 1)
  {
    currentThread->state = DEAD;
    switch_thread();
    return;
  }
  currentThread->stackFrameArraySize--;
  stackFrame--;
  pc = stackFrame->pc;
  stackTop = stackFrame->stackTop;
  localsBase = stackFrame->localsBase;
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
 LABEL_INSTANCE:
  if (rtType == classIndex)
    return 1;
  if (rtType == JAVA_LANG_OBJECT)
    return 0;
  rtType = get_class_record(rtType)->parentClass;
  goto LABEL_INSTANCE;
}





