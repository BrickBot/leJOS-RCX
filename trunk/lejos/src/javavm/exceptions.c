
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
Object *noSuchMethodError = null;
Object *stackOverflowError = null;
Object *nullPointerException = null;
Object *classCastException = null;
Object *arithmeticException = null;

void init_exceptions()
{
  outOfMemoryError = new_object_for_class (JAVA_LANG_OUTOFMEMORYERROR);
  noSuchMethodError = new_object_for_class (JAVA_LANG_NOSUCHMETHODERROR);
  stackOverflowError = new_object_for_class (JAVA_LANG_STACKOVERFLOWERROR);
  nullPointerException = new_object_for_class (JAVA_LANG_NULLPOINTEREXCEPTION);
  classCastException = new_object_for_class (JAVA_LANG_CLASSCASTEXCEPTION);
  arithmeticException = new_object_for_class (JAVA_LANG_ARITHMETICEXCEPTION);
}

void throw_exception_checked (Object *exception)
{
  if (exception == null)
  {
    throw_exception (nullPointerException);
    return;
  }
  throw_exception (exception);
}

/**
 * @return false iff all threads are dead.
 */
void throw_exception (Object *exception)
{
  TWOBYTES currentOffset;
  MethodRecord *methodRecord = null;
  StackFrame *stackFrame;
  ExceptionRecord *exceptionRecord;
  byte numExceptionHandlers;
  MethodRecord *exceptionMr = null;
  #ifdef EMULATE
  byte *exceptionPc = pc;
  #endif

  #ifdef VERIFY
  assert (exception != null, EXCEPTIONS0);
  #endif

 LABEL_PROPAGATE:
  if (currentThread->state == DEAD)
  {
    #ifdef EMULATE
    printf ("*** UNCAUGHT EXCEPTION: \n");
    printf ("--  Exception class   : %d\n", (int) get_class_index (exception));
    printf ("--  Thread            : %d\n", (int) currentThread->threadId);
    printf ("--  Method signature  : %d\n", (int) exceptionMr->signatureId);
    printf ("--  Root method sig.  : %d\n", (int) methodRecord->signatureId);
    printf ("--  Bytecode offset   : %d\n", (int) exceptionPc - 
            (int) get_code_ptr(exceptionMr));
    #else
    
    // TBD: 

    #endif EMULATE
    return;
  }
  stackFrame = current_stackframe();
  methodRecord = stackFrame->methodRecord;
  if (exceptionMr == null)
    exceptionMr = methodRecord;
  exceptionRecord = (ExceptionRecord *) (get_binary_base() + methodRecord->exceptionTable);
  currentOffset = (TWOBYTES) (pc - (get_binary_base() + methodRecord->codeOffset));
  numExceptionHandlers = methodRecord->numExceptionHandlers;
  while (numExceptionHandlers--)
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




