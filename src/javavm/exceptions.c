
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
#include "stack.h"

Object *outOfMemoryError;
Object *noSuchMethodError;
Object *stackOverflowError;
Object *nullPointerException;
Object *classCastException;
Object *arithmeticException;
Object *arrayIndexOutOfBoundsException;

// Temporary globals:

static TWOBYTES tempCurrentOffset;
static MethodRecord *tempMethodRecord = null;
static StackFrame *tempStackFrame;
static ExceptionRecord *gExceptionRecord;
static byte gNumExceptionHandlers;
static MethodRecord *gExcepMethodRec = null;
#ifdef EMULATE
static byte *gExceptionPc;
#endif

void init_exceptions()
{
  outOfMemoryError = new_object_for_class (JAVA_LANG_OUTOFMEMORYERROR);
  noSuchMethodError = new_object_for_class (JAVA_LANG_NOSUCHMETHODERROR);
  stackOverflowError = new_object_for_class (JAVA_LANG_STACKOVERFLOWERROR);
  nullPointerException = new_object_for_class (JAVA_LANG_NULLPOINTEREXCEPTION);
  classCastException = new_object_for_class (JAVA_LANG_CLASSCASTEXCEPTION);
  arithmeticException = new_object_for_class (JAVA_LANG_ARITHMETICEXCEPTION);
  arrayIndexOutOfBoundsException = new_object_for_class (JAVA_LANG_ARRAYINDEXOUTOFBOUNDSEXCEPTION);
}

/**
 * @return false iff all threads are dead.
 */
void throw_exception (Object *exception)
{
  #ifdef VERIFY
  assert (exception != null, EXCEPTIONS0);
  #endif
  #if EMULATE
  gExceptionPc = pc;
  #endif
  gExcepMethodRec = null;

  #if 0
  trace (-1, get_class_index(exception), 3);
  #endif

 LABEL_PROPAGATE:
  if (currentThread->state == DEAD)
  {
    // TBD: There seems to be a problem here: do_return must have
    // switched the currentThread already.

    #ifdef EMULATE
    printf ("*** UNCAUGHT EXCEPTION: \n");
    printf ("--  Exception class   : %d\n", (int) get_class_index (exception));
    printf ("--  Thread            : %d\n", (int) currentThread->threadId);
    printf ("--  Method signature  : %d\n", (int) gExcepMethodRec->signatureId);
    printf ("--  Root method sig.  : %d\n", (int) tempMethodRecord->signatureId);
    printf ("--  Bytecode offset   : %d\n", (int) gExceptionPc - 
            (int) get_code_ptr(gExcepMethodRec));
    #else
    
    trace (4, gExcepMethodRec->signatureId, get_class_index (exception) % 10);

    #endif EMULATE
    return;
  }
  tempStackFrame = current_stackframe();
  tempMethodRecord = tempStackFrame->methodRecord;

  if (gExcepMethodRec == null)
    gExcepMethodRec = tempMethodRecord;
  gExceptionRecord = (ExceptionRecord *) (get_binary_base() + tempMethodRecord->exceptionTable);
  tempCurrentOffset = ptr2word(pc) - ptr2word(get_binary_base() + tempMethodRecord->codeOffset);

  #if 0
  trace (-1, tempCurrentOffset, 5);
  #endif

  gNumExceptionHandlers = tempMethodRecord->numExceptionHandlers;
  while (gNumExceptionHandlers--)
  {
    if (tempCurrentOffset >= gExceptionRecord->start && tempCurrentOffset <= gExceptionRecord->end)
    {
      // Check if exception class applies
      if (instance_of (exception, gExceptionRecord->classIndex))
      {
        // Clear operand stack
        init_stack_ptr (tempStackFrame, tempMethodRecord);
        // Push the exception object
        push_ref (ptr2word (exception));
        // Jump to handler:
        pc = get_binary_base() + tempMethodRecord->codeOffset + 
             gExceptionRecord->handler;
        return;
      }
    }
    gExceptionRecord++;
  }
  // No good handlers in current stack frame - go up.
  do_return (0);
  // Note: return takes care of synchronized methods.
  goto LABEL_PROPAGATE; 
}




