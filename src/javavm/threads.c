
#include "classes.h"
#include "types.h"
#include "constants.h"
#include "language.h"
#include "configure.h"

#define NO_OWNER 0

#define get_stack_frame() ((StackFrame *) (currentThread->currentStackFrame))

/**
 * Thread currently being executed by engine().
 */
#ifdef SAFE
static Thread* currentThread = null;
#else
static Thread* currentThread;
#endif

void init_thread (Thread *thread)
{
  thread->stackFrameArray = new_primitive_array (T_STACKFRAME, MAX_STACK_FRAMES);
  thread->stackArray = new_primitive_array (T_INTEGER, STACK_SIZE);
  thread->currentStackFrame = null;
  thread->state = STARTED;
  if (currentThread == null)
  {
    currentThread = thread;
    thread->nextThread = thread;
  }
  thread->nextThread = currentThread->nextThread;
  currentThread->nextThread = thread;
}

/**
 * Switches to next thread.
 * Notes:
 */
void switch_thread()
{
  // TBD: loops forever when all threads are dead.

  Thread *nextThread;
  StackFrame *stackFrame;

  #ifdef VERIFY
  assert (currentThread != null, THREADS0);
  #endif

  // Save context information
  stackFrame = get_stack_frame();
  stackFrame->pc = pc;
  stackFrame->stackTop = stackTop;
  // Loop until a RUNNING frame is found
 LABEL_TASKLOOP:
  nextThread = currentThread->nextThread;
  if (nextThread->state == WAITING)
  {
    if (get_thread_id((Object *) (nextThread->waitingOn)) == NO_OWNER)
    {
      nextThread->state = RUNNING;
      #ifdef SAFE
      nextThread->waitingOn = null;
      #endif
    }
  }

  #if REMOVE_DEAD_THREADS
  if (nextThread->state == DEAD)
  {
    free_array (nextThread->stackFrameArray);
    free_array (nextThread->stackArray);

    #ifdef SAFE
    nextThread->stackFrameArray = null;
    nextThread->stackArray = null;
    #endif SAFE

    nextThread = nextThread->nextThread;
    currentThread->nextThread = nextThread;
  }
  #endif REMOVE_DEAD_THREADS

  currentThread = nextThread;
  if (currentThread->state == STARTED)
  {
    currentThread->state = RUNNING;
    dispatch_virtual ((Object *) currentThread, RUN_V);
  }
  if (currentThread->state != RUNNING)
    goto LABEL_TASKLOOP;
  stackFrame = get_stack_frame();
  pc = stackFrame->pc;
  stackTop = stackFrame->stackTop;
  localsBase = stackFrame->localsBase;
}

/**
 * thread enters obj's monitor.
 * Note that this operation is atomic as far as the program is concerned.
 */
void enter_monitor (Object* obj)
{
  byte owner;
  byte tid;
  owner = get_thread_id (obj);
  tid = currentThread->threadId;
  if (owner != NO_OWNER && tid != owner)
  {
    currentThread->state = WAITING;
    currentThread->waitingOn = obj;
    // Gotta yield
    switch_thread();
    return;
  }
  set_thread_id (obj, tid);
  inc_monitor_count (obj);
}

void exit_monitor (Object* obj)
{
  #ifdef VERIFY
  assert (get_thread_id(obj) == currenThread->threadId, THREADS1);
  assert (get_monitor_count(obj) > 0, THREADS2);
  #endif
  byte newMonitorCount = get_monitor_count(obj)-1;
  if (newMonitorCount == 0)
    set_thread_id (obj, NO_OWNER);
  set_monitor_count (obj, newMonitorCount);
}





