#include "exceptions.h"
#include "types.h"
#include "classes.h"

// TBD: Exception in synchronized blocks?

/**
 * @return True iff caught somewhere.
 */
boolean trow_exception (Object *exception)
{
  TWOBYTES currentOffset;

 LABEL_PROPAGATE:
  if (currentThread->state == DEAD)
    return false;
  StackFrame *stackFrame = (StackFrame *) currentThread->currentStackFrame;
  MethodRecord *methodRecord = stackFrame->methodRecord;
  ExceptionRecord *exceptionRecord = (ExceptionRecord *) (binary_base() + methodRecord->exceptionTable);
  currentOffset = pc - (binary_base() + methodRecord->codeOffset);
  int numExceptions = methodRecord->numExceptions;
  for (int i = 0; i < numExceptions; i++)
  {
    if (currentOffset >= exceptionRecord->start && currentOffset <= exceptionRecord->end)
    {
      // Check if exception class applies
      if (instance_of (exception, exceptionRecord->classIndex))
      {
        // TBD: operand stack is supposed to be clared??

        pc = binary_base() + methodRecord->codeOffset + 
             exceptionRecord->handler;
        return true;
      }
    }
    exceptionRecord++;
  }
  // No good handlers in current stack frame - go up
  do_return (0); 
  goto LABEL_PROPAGATE; 
}



