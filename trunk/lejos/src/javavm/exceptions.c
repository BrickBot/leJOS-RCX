
#include "exceptions.h"
#include "types.h"
#include "classes.h"
#include "threads.h"

// TBD: Exception in synchronized blocks?

/**
 * @return false iff all threads are dead.
 */
void trow_exception (Object *exception)
{
  TWOBYTES currentOffset;
  int numExceptions;
  MethodRecord *methodRecord;
  StackFrame *stackFrame;
  ExceptionRecord *exceptionRecord;

 LABEL_PROPAGATE:
  if (currentThread->state == DEAD)
    return;
  stackFrame = (StackFrame *) currentThread->currentStackFrame;
  methodRecord = stackFrame->methodRecord;
  exceptionRecord = (ExceptionRecord *) (binary_base() + methodRecord->exceptionTable);
  currentOffset = pc - (binary_base() + methodRecord->codeOffset);
  numExceptions = methodRecord->numExceptions;
  for (int i = 0; i < numExceptions; i++)
  {
    if (currentOffset >= exceptionRecord->start && currentOffset <= exceptionRecord->end)
    {
      // Check if exception class applies
      if (instance_of (exception, exceptionRecord->classIndex))
      {
        // Clear operand stack:
        stackTop = stackFrame->localsBase + methodRecord->numLocals - 1;
        stackFrame->stackTop = stackTop;
        // Jump to handler:
        pc = binary_base() + methodRecord->codeOffset + 
             exceptionRecord->handler;
        stackFrame->pc = pc;
        return;
      }
    }
    exceptionRecord++;
  }
  // No good handlers in current stack frame - go up
  do_return (0);
  goto LABEL_PROPAGATE; 
}




