
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

typedef struct S_StackFrame
{
  MethodRecord *methodRecord;
  STACKWORD *localsBase;
  STACKWORD *stackTop;
  byte *pc;
  Object *monitor;
} StackFrame;

extern void start_thread (Thread *thread);
extern void init_thread (Thread *thread);
extern void switch_thread();
extern StackFrame *current_stackframe();
extern void enter_monitor (Object* obj);
extern void exit_monitor (Object* obj);

#define stackframe_array()   ((StackFrame *) ((byte *) currentThread->stackFrameArray + HEADER_SIZE))
#define stack_array()        ((STACKWORD *) ((byte *) currentThread->stackArray + HEADER_SIZE))

#endif



