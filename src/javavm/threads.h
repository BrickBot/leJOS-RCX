#ifndef _THREADS_H
#define _THREADS_H

#include "classes.h"

#define RUNNING 0
#define WAITING 1
#define DEAD    2
#define STARTED 3

extern Thread *currentThread;

extern void start_thread (Thread *thread);
extern void init_thread (Thread *thread);
extern void switch_thread();

typedef struct S_StackFrame
{
  MethodRecord *methodRecord;
  FOURBYTES *localsBase;
  FOURBYTES *stackTop;
  byte *pc;
} StackFrame;

inline StackFrame *current_stack_frame()   
{
  return ((StackFrame *) currentThread->currentStackFrame);
}

inline MethodRecord *current_method()
{
  return current_stack_frame()->methodRecord;
}

#endif
