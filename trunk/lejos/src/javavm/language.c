**
 * The language datastructures.
 */

#include "language.h"
#include "types.h"
#include "classes.h"
#include "specialclasses.h"
#include "specialsignatures.h"

#define F_SIZE_MASK    0xE0
#define F_SIZE_SHIFT   5
#define F_OFFSET_MASK  0x1F

ClassRecord* classesBinary;

#ifdef VERIFY
boolean classesInitialized = false;
#endif

#define get_stack_object(MREC_)  ((Object *) *(stackTop - (MREC_)->numParameters + 1))

/**
 * Puts or gets field.
 */
void handle_field (byte hiByte, byte loByte, boolean doPut, boolean aStatic) 
  byte *sourcePtr;
  byte *destPtr;
  byte *fieldBase;
  TWOBYTES offset;

  // TBD: Check for class initialization
  // TBD: byte alignment not right
  
  fieldSize = (hiByte >> F_SIZE_SHIFT) + 1;
  offset = (TWOBYTES) (hiByte & F_OFFSET_MASK) * 0x100 + loByte;
  numWordsMinus1 = (fieldSize <= 4) ? 0 : 1;
  fieldBase = (aStatic ? get_static_base() : (byte *) mk_pobject(*stackTop))
              + offset;  
  if (doPut)
  {
    stackTop -= numWordsMinus1;
    sourcePtr = (byte *) stackTop;
    // Pop the object reference
    stackTop--;
    destPtr = fieldBase;
  }  
  else
  {
    sourcePtr = fieldBase;
    destPtr = (byte *) stackTop;
    stackTop += numWordsMinus1;
    // Clear one word in the stack
    *((FOURBYTES *) destPtr) = 0L;
  }
  // Copy state byte for byte
  for (ctr = fieldSize; ctr-- > 0;)
    *destPtr++ = sourcePtr++;    
}

/**
 * @return Method index or -1.
 */
short find_method (byte classIndex, TWOBYTES methodSignature)
{
  ClassRecord *classRecord;
  MethodRecord *methodRecord;
  byte i;

  classRecord = get_class_record(classIndex);
  for (i = 0; i < classRecord->numMethods; i++)
  {
    methodRecord = get_method_record(classRecord,i);
    if (methodRecord->signatureId == methodSignature)
      return (short) i;
    // TBD: Check if cast preservs sign
  }
  return -1;
}

void dispatch_virtual (Object *ref, TWOBYTES signature)
{
 LABEL_METHODLOOKUP:
  classIndex = get_class_index(ref);
  methodIndex = find_method (classIndex, signature);
  if (methodIndex == -1)
  {
    if (classIndex == JAVA_LANG_OBJECT)
    {
      throw_exception (...);
      return;
    }
    classIndex = get_class_record(classIndex)->parentClass;
    goto LABEL_METHODLOOP;
  }
  dispatch_special (classIndex, methodIndex);
}

/**
 * @param hiByte Class index.
 * @param loByte Method index.
 */
void dispatch_special (byte classIndex, byte methodIndex)
{
  ClassRecord *classRecord;
  MethodRecord *methodRecord;
  StackFrame *stackFrame;
  Object *obj;
  boolean isStatic;

  classRecord = get_class_record(classIndex);
  if (!initialized (classRecord))
  {
    set_initialized (classRecord);
    // Note that pc hasn't moved yet
    // TBD: Case where static initializer throws an exception
    // TBD: Does every class have a static initializer?
    dispatch_special (classIndex, STATIC_INITIALIZER);
    return;
  }
  methodRecord = ((MethodRecord *) (binary_base() + classRecord->methodTableOffset)) + methodIndex;
  // Update stack and pc of caller
  pc += 3;
  stackTop -= methodRecord->numParameters;
  // Get the receiver
  // TBD: Handle null objects (throw exception before pc is updated)
  if (!(isStatic = is_static (methodRecord)))
    obj = get_stack_object(methodRecord);
  // Handle native methods
  if (is_native (methodRecord))
  {
    dispatch_native (obj, methodRecord);
    return;
  }
  stackFrame = (StackFrame *) currentThread->currentStackFrame;
  // TBD: native
  // TBD: Handle stackFrame == null
  // Save current context in current stack frame
  stackFrame->pc = pc;
  stackFrame->stackTop = stackTop;
  // Get some info from calling method
  callingMethod = stackFrame->methodRecord;
  // Note that some of the operand stack of the caller is used by the callee.
  newLocalsBase = stackFrame->localsBase + callingMethod->numLocals + callingMethod->maxOperands - methodRecord->numParameters;
  stackFrame++;
  #if STACK_CHECKING
  if (stackFrame - ((StackFrame *) currentThread->stackFrameArray) >= MAX_STACK_FRAMES)
  {
    throw_exception (...);
    // TBD: Cleanup here?
    return;
  }
  #endif
  // Initialize new stack frame
  stackFrame->localsBase = newLocalsBase;
  stackFrame->methodRecord = methodRecord;
  // TBD: Initialize locals
  // Note that stackTop always points to the top word in the stack
  localsBase = newLocalsBase;
  stackTop = newLocalsBase + methodRecord->numLocals - 1;
  pc = binary_base() + methodRecord->codeOffset;  
  // Check synchronized methods
  if (is_synchronized(methodRecord) && !isStatic)
  {
    stackFrame->monitor = obj;
    enter_monitor (obj);
  }
  else
  {
    stackFrame->monitor = null;
  }
}

void do_return (byte numWords)
{
  StackFrame *stackFrame;
  STACKWORD *sourcePtr;
  // Place sourcePtr below data to be copied up the stack
  sourcePtr = stackTop - numWords;
  stackFrame = (StackFrame *) currentThread->currentStackFrame;
  #ifdef VERIFY
  assert (stackFrame != null, LANGUAGE7);
  #endif
  if (stackFrame->monitor != null)
    exit_monitor (stackFrame->monitor);
  if (is_first_stackframe (stackFrame))
  {
    currentThread->state = DEAD;
    switch_thread();
    return;
  }
  currentThread->currentStackFrame = (REFERENCE) --stackFrame;
  pc = stackFrame->pc;
  stackTop = stackFrame->stackTop;
  localsBase = stackFrame->localsBase;
  for (i = numWords; i-- > 0;)
  {
    *(++stackTop) = *(++sourcePtr);
  }  
}

/**
 * @return 1 or 0.
 */
TWOBYTES instance_of (Object *obj, byte classIndex)
{
  byte rtType;
  rtType = get_class_index(obj);
 LABEL_INSTANCE:
  if (rtType == classIndex)
    return (TWOBYTES) 1;
  if (rtType == JAVA_LANG_OBJECT)
    return (TWOBYTES) 0;
  rtType = get_class_record(rtType)->parentClass;
  goto LABEL_INSTANCE;
}

