
#include "classes.h"
#include "language.h"

#ifndef _THREADS_H
#define _THREADS_H

#define RUNNING 0
#define WAITING 1
#define DEAD    2
#define STARTED 3

#define SF_SIZE (sizeof(StackFrame))

extern Thread *currentThread;
extern Thread *bootThread;
extern byte gThreadCounter;

typedef struct S_StackFrame
{
  MethodRecord *methodRecord;
  Object *monitor;
  STACKWORD *localsBase;
  STACKWORD *stackTop;
  boolean *isReferenceBase;
  boolean *isReference;
  byte *pc;
} StackFrame;

extern void start_thread (Thread *thread);
extern void init_thread (Thread *thread);
extern void switch_thread();
extern StackFrame *current_stackframe();
extern void enter_monitor (Object* obj);
extern void exit_monitor (Object* obj);

/**
 * Must be written by user of threads.h.
 */
extern void switch_thread_hook();

#define stackframe_array_ptr()   (word2ptr(currentThread->stackFrameArray))
#define stack_array_ptr()        (word2ptr(currentThread->stackArray))
#define is_reference_array_ptr() (word2ptr(currentThread->isReferenceArray))
#define stackframe_array()       ((StackFrame *) ((byte *) stackframe_array_ptr() + HEADER_SIZE))
#define stack_array()            ((STACKWORD *) ((byte *) stack_array_ptr() + HEADER_SIZE))
#define is_reference_array()     ((JBYTE *) ((byte *) is_reference_array_ptr() + HEADER_SIZE))

#endif



