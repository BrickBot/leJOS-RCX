
#include "types.h"
#include "trace.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "exceptions.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "memory.h"

Object *outOfMemoryError = null;

void init_exceptions()
{
  outOfMemoryError = new_object_for_class (JAVA_LANG_OUTOFMEMORYERROR);
}

/**
 * @return false iff all threads are dead.
 */
void trow_exception (Object *exception)
{
  TWOBYTES currentOffset;
  MethodRecord *methodRecord;
  StackFrame *stackFrame;
  ExceptionRecord *exceptionRecord;
  byte numExceptionHandlers;
  byte i;

  #ifdef VERIFY
  assert (exception != null, EXCEPTIONS0);
  #endif

 LABEL_PROPAGATE:
  if (currentThread->state == DEAD)
    return;
  stackFrame = (StackFrame *) currentThread->currentStackFrame;
  methodRecord = stackFrame->methodRecord;
  exceptionRecord = (ExceptionRecord *) (get_binary_base() + methodRecord->exceptionTable);
  currentOffset = (TWOBYTES) (pc - (get_binary_base() + methodRecord->codeOffset));
  numExceptionHandlers = methodRecord->numExceptionHandlers;
  for (i = 0; i < numExceptionHandlers; i++)
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
        pc = get_binary_base() + methodRecord->codeOffset + 
             exceptionRecord->handler;
        stackFrame->pc = pc;
        return;
      }
    }
    exceptionRecord++;
  }
  // No good handlers in current stack frame - go up.
  do_return (0);
  // Note: return takes care of synchronized methods.
  goto LABEL_PROPAGATE; 
}




