
#include "types.h"
#include "trace.h"
#include "platform_hooks.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "memory.h"
#include "exceptions.h"
#include "stack.h"

#define NO_OWNER 0x00

#define get_stack_frame() ((StackFrame *) (currentThread->currentStackFrame))

/**
 * Thread currently being executed by engine(). Threads exist in an
 * intrinsic circular list.
 */
Thread* currentThread;

/**
 * Priority queue of threads. Entry points at the last thread in the queue.
 */
Thread *threadQ[10];

/**
 * Thread id generator, always increasing.
 */
byte gThreadCounter;

/**
 * Current program number, i.e. number of 'main()'s hanging around
 */
byte gProgramNumber;

StackFrame *current_stackframe()
{
  byte arraySize;

  arraySize = currentThread->stackFrameArraySize;
  if (arraySize == 0)
    return null;
  return stackframe_array() + (arraySize - 1);
}

void update_stack_frame (StackFrame *stackFrame)
{
  stackFrame->stackTop = stackTop;
  stackFrame->pc = pc;
}  

void update_registers (StackFrame *stackFrame)
{
  pc = stackFrame->pc;
  stackTop = stackFrame->stackTop;
  localsBase = stackFrame->localsBase;
}

inline byte get_thread_id (Object *obj)
{
  return obj->threadId;
}

inline void set_thread_id (Object *obj, byte threadId)
{
  obj->threadId = threadId;
}

inline void inc_monitor_count (Object *obj)
{
  obj->monitorCount++;
}

inline void set_monitor_count (Object *obj, byte count)
{
  obj->monitorCount = count;
}

/**
 * Allocate stack frames
 * Allocate ID
 * Insert into run list
 * Mark thread as STARTED
 */
boolean init_thread (Thread *thread)
{
  thread->threadId = gThreadCounter + 1;
  
  // Catch the primordial thread
  if (currentThread == null)
    thread->priority = NORM_PRIORITY;
  
  #if DEBUG_THREADS
  printf("Setting intial priority to %d\n", thread->priority);
  #endif

/*  
  if (thread->state != NEW || thread->threadId == NO_OWNER)
  {
    // Thread already initialized?
    // This assumes object creation sets state field to zero (NEW).
    throw_exception (outOfMemoryError);
    return false;
  }
*/
  if (thread->state != NEW)
  {
    throw_exception(illegalStateException);
    return false;
  }
  
  // Allocate space for stack frames. Can only recurse MAX_STACK_FRAMES times.
  thread->stackFrameArray = ptr2word (new_primitive_array (T_STACKFRAME, MAX_STACK_FRAMES));
  if (thread->stackFrameArray == JNULL)
    return false;
    
  // Allocate actual stack storage (STACK_SIZE * 4 bytes)
  thread->stackArray = ptr2word (new_primitive_array (T_INT, STACK_SIZE));
  if (thread->stackArray == JNULL)
  {
    free_array (ref2obj(thread->stackFrameArray));
    thread->stackFrameArray = JNULL;
    return false;    
  }
  
  gThreadCounter++;
  
  #ifdef VERIFY
  assert (is_array (word2obj (thread->stackFrameArray)), THREADS0);
  assert (is_array (word2obj (thread->stackArray)), THREADS1);
  #endif

  thread->stackFrameArraySize = 0;
  thread->state = STARTED;
  if (currentThread == null)
    currentThread = thread;
    
  enqueue_thread(thread);

  return true;
}

/**
 * Switches to next thread:
 *
 * do
 *   get next thread
 *   if waiting, grab monitor and run
 *   if sleeping and timer expired, run
 *   if DEAD, clean up and use current thread
 *   if started, initialize and run
 * until selected thread can run
 *  
 * @return false iff there are no live threads
 *         to switch to.
 */
boolean switch_thread()
{
  Thread *anchorThread, *previousThread, *threadToRun = null, *lastRunnableThread = null;
  Thread **pThreadQ;
  boolean interruptMe = false;
  boolean nonDaemonRunnable = false;
  StackFrame *stackFrame;
  int i;

  #if DEBUG_THREADS || DEBUG_BYTECODE
  printf ("------ switch_thread: currentThread at %d\n", (int) currentThread);
  #endif

  #ifdef VERIFY
  assert (currentThread != null, THREADS4);
  #endif

  // Save context information
  stackFrame = current_stackframe();

  #if DEBUG_THREADS
  printf ("switchThread: current stack frame: %d\n", (int) stackFrame);
  #endif
  
  #ifdef VERIFY
  assert (stackFrame != null || currentThread->state == STARTED,
          THREADS5);
  #endif

  if (stackFrame != null)
  {
    update_stack_frame (stackFrame);
  }

  do
  {
    // Loop until a frame is found that can be made to run.
    for (i=MAX_PRIORITY-1, pThreadQ=&threadQ[MAX_PRIORITY-1]; i >= 0; i--, pThreadQ--)
    {
      previousThread = anchorThread = *pThreadQ;
      if (!previousThread)
        continue;
        
      do
      {
        currentThread = word2ptr(previousThread->nextThread);
      
        #if DEBUG_THREADS
        printf ("Calling switch_thread_hook\n");
        #endif
        switch_thread_hook();
        if (gMakeRequest && gRequestCode == REQUEST_EXIT)
          return false;
        #if DEBUG_THREADS
        printf ("Checking state of thread %d(%d)(s=%d,p=%d,i=%d,d=%d)\n",
        	(int)currentThread,
        	(int)currentThread->threadId,
        	(int)currentThread->state,
        	(int)currentThread->priority,
        	(int)currentThread->interrupted,
        	(int)currentThread->daemon
               );
        #endif
        switch (currentThread->state)
        {
          case RUNNING:
            // We were set to running at some point. That's OK.
            break;
          case CONDVAR_WAITING:
            // We are waiting to be notified
            if ((currentThread->sleepUntil > 0) && (get_sys_time() >= (FOURBYTES) currentThread->sleepUntil))
            {
#if DEBUG_MONITOR
        printf ("Waking up waiting thread %d: %d\n", (int) currentThread, currentThread->threadId);
#endif
                currentThread->state = MON_WAITING;
            }
            else if (!currentThread->interrupted)
            	break;
            else
            	interruptMe = true;
            // drop through
          case MON_WAITING:
            // We are waiting to enter a synchronized block
            #ifdef VERIFY
            assert (currentThread->waitingOn != JNULL, THREADS6);
            #endif
    
            if (get_thread_id (word2obj (currentThread->waitingOn)) == NO_OWNER)
            {
              // NOW enter the monitor (guaranteed to succeed)
              enter_monitor (word2obj (currentThread->waitingOn));
              
              // Set the monitor depth to whatever was saved.
              set_monitor_count(word2obj(currentThread->waitingOn), currentThread->monitorCount);
              
              // Let the thread run.
              currentThread->state = RUNNING;

              #ifdef SAFE
              currentThread->waitingOn = JNULL;
              #endif
            }
            break;
          case SLEEPING:
            if (!threadToRun && (currentThread->interrupted || (get_sys_time() >= (FOURBYTES) currentThread->sleepUntil)))
            {
        #if DEBUG_THREADS
        printf ("Waking up sleeping thread %d: %d\n", (int) currentThread, currentThread->threadId);
        #endif
              currentThread->state = RUNNING;
              if (currentThread->interrupted)
                interruptMe = true;
              #ifdef SAFE
    	    currentThread->sleepUntil = JNULL;
              #endif // SAFE
            }
            break;
          case DEAD:
        #if DEBUG_THREADS
        printf ("Tidying up DEAD thread %d: %d\n", (int) currentThread, (int)currentThread->threadId);
        #endif
    
            #if REMOVE_DEAD_THREADS
            // This order of deallocation is actually crucial to avoid leaks
            free_array ((Object *) word2ptr (currentThread->stackArray));
            free_array ((Object *) word2ptr (currentThread->stackFrameArray));
    
            #ifdef SAFE
            currentThread->stackFrameArray = JNULL;
            currentThread->stackArray = JNULL;
            #endif // SAFE
            #endif // REMOVE_DEAD_THREADS
          
            // Remove thread from queue.
            dequeue_thread(currentThread);
            if (currentThread == anchorThread)
              anchorThread = previousThread;
              
            currentThread = previousThread;     
            break;
          case STARTED:      
            // Put stack ptr at the beginning of the stack so we can push arguments
            // to entry methods. This assumes set_top_word or set_top_ref will
            // be called immediately below.
        #if DEBUG_THREADS
        printf ("Starting thread %d: %d\n", (int) currentThread, currentThread->threadId);
        #endif
            init_sp_pv();
            currentThread->state = RUNNING;
            if (currentThread == bootThread)
            {
              ClassRecord *classRecord;
    
              classRecord = get_class_record (get_entry_class (gProgramNumber));
              // Initialize top word with fake parameter for main():
              set_top_ref (JNULL);
              // Push stack frame for main method:
              dispatch_special (find_method (classRecord, main_4_1Ljava_3lang_3String_2_5V), null);
              // Push another if necessary for the static initializer:
              dispatch_static_initializer (classRecord, pc);
            }
            else
            {
              set_top_ref (ptr2word (currentThread));
              dispatch_virtual ((Object *) currentThread, run_4_5V, null);
            }
            // The following is needed because the current stack frame
            // was just created
            stackFrame = current_stackframe();
            update_stack_frame (stackFrame);
            break;
          default:
            // Shouldn't get here. Not much we can do about it though.
            break;
        }
        #if DEBUG_THREADS
        printf ("switch_thread: done processing thread %d: %d\n", (int) currentThread,
                (int) (currentThread->state == RUNNING));
        #endif
  
        // May not be any more threads left of priority 'i' but
        // currentThread should always point at a valid thread.
        if (*pThreadQ == null) {
          currentThread = lastRunnableThread;
          break;
        }
        
        // We've got at least one thread that isn't DEAD.
        if (currentThread->state != DEAD)
        {
          // If it isn't a daemon thread we won't be terminating
          if (!currentThread->daemon)
          {
#if DEBUG_THREADS
  printf ("Found a non-daemon thread %d: %d(%d)\n", (int) currentThread, (int)currentThread->threadId, (int) currentThread->state);
#endif
             nonDaemonRunnable = true;
          }

          // May need it later             
          lastRunnableThread = currentThread;
        }
        
        // Always use the first running thread as the thread
        if ((currentThread->state == RUNNING) && (threadToRun == null))
        {
          threadToRun = currentThread;
          // Move thread to end of queue
          *pThreadQ = currentThread;
        }
        
        // Keep looping: cull dead threads, check there's at least one non-daemon thread
        previousThread = currentThread;   
      } while (previousThread != anchorThread);
    } // end for
    
#if DEBUG_THREADS
  printf ("threadToRun=%d, ndr=%d\n", (int) threadToRun, (int)nonDaemonRunnable);
#endif
    // If no thread can be run yet and there is at least one non-daemon thread
    // keep looping.
  } while ((threadToRun == null) && nonDaemonRunnable);

#if DEBUG_THREADS
  printf ("Leaving switch_thread()\n");
#endif
  // If we found a running thread and there is at least one
  // non-daemon thread left somewhere in the queue...
  if ((threadToRun != null) && nonDaemonRunnable)
  {  
    currentThread = threadToRun;
    #if DEBUG_THREADS
    printf ("Current thread is %d: %d(%d)\n", (int) currentThread, (int)currentThread->threadId, (int) currentThread->state);
    printf ("getting current stack frame...\n");
    #endif
  
    stackFrame = current_stackframe();
  
    #if DEBUG_THREADS
    printf ("updating registers...\n");
    #endif
  
    update_registers (stackFrame);
  
    #if DEBUG_THREADS
    printf ("done updating registers\n");
    #endif
  
    if (interruptMe)
      throw_exception(interruptedException);
    
    return true;
  }
  
  currentThread = null;
  return false;
}

/*
 * Current thread owns object's monitor (we hope) and wishes to relinquish
 * it temporarily (by calling Object.wait()).
 */
void monitor_wait(Object *obj, const FOURBYTES time)
{
#if DEBUG_MONITOR
  printf("monitor_wait of %d, thread %d(%d)\n",(int)obj, (int)currentThread, currentThread->threadId);
#endif
  if (currentThread->threadId != get_thread_id (obj))
  {
    throw_exception(illegalMonitorStateException);
    return;
  }
  
  // Great. We own the monitor which means we can give it up, but
  // indicate that we are listening for notify's.
  currentThread->state = CONDVAR_WAITING;
  
  // Save monitor depth
  currentThread->monitorCount = get_monitor_count(obj);
  
  // Save the object who's monitor we will want back
  currentThread->waitingOn = ptr2word (obj);
  
  // Might be an alarm set too.
  if (time > 0)
    currentThread->sleepUntil = get_sys_time() + time; 	
  else
    currentThread->sleepUntil = 0;
  
#if DEBUG_MONITOR
  printf("monitor_wait of %d, thread %d(%d) until %ld\n",(int)obj, (int)currentThread, currentThread->threadId, time);
#endif

  // Indicate that the object's monitor is now free.
  set_thread_id (obj, NO_OWNER);
  set_monitor_count(obj, 0);
  
  // Gotta yield
  switch_thread();
}

/*
 * Current thread owns object's monitor (we hope) and wishes to wake up
 * any other threads waiting on it. (by calling Object.notify()).
 */
void monitor_notify(Object *obj, const boolean all)
{
  int i;
  Thread *pThread;
  
#if DEBUG_MONITOR
  printf("monitor_notify of %d, thread %d(%d)\n",(int)obj, (int)currentThread, currentThread->threadId);
#endif
  if (currentThread->threadId != get_thread_id (obj))
  {
    throw_exception(illegalMonitorStateException);
    return;
  }

  // Find a thread waiting on us and move to WAIT state.
  for (i=MAX_PRIORITY-1; i >= 0; i--)
  {
    pThread = threadQ[i];
    if (!pThread)
      continue;
      
    do {
      // Remember threadQ[i] is the last thread on the queue
      pThread = word2ptr(pThread->nextThread);
      if (pThread->state == CONDVAR_WAITING && pThread->waitingOn == ptr2word (obj))
      {
        pThread->state = MON_WAITING;
        if (!all)
          return;
      }
    } while (pThread != threadQ[i]);
  }
}

/**
 * currentThread enters obj's monitor:
 *
 * if monitor is in use, save object in thread and re-schedule
 * else grab monitor and increment its count.
 * 
 * Note that this operation is atomic as far as the program is concerned.
 */
void enter_monitor (Object* obj)
{
#if DEBUG_MONITOR
  printf("enter_monitor of %d\n",(int)obj);
#endif

  if (obj == JNULL)
  {
    throw_exception (nullPointerException);
    return;
  }

  if (get_monitor_count (obj) != NO_OWNER && currentThread->threadId != get_thread_id (obj))
  {
    // There is an owner, but its not us.
    // Make thread wait until the monitor is relinquished.
    currentThread->state = MON_WAITING;
    currentThread->waitingOn = ptr2word (obj);
    currentThread->monitorCount = 1;
    // Gotta yield
    schedule_request (REQUEST_SWITCH_THREAD);    
    return;
  }
  set_thread_id (obj, currentThread->threadId);
  inc_monitor_count (obj);
}

/**
 * Decrement monitor count
 * Release monitor if count reaches zero
 */
void exit_monitor (Object* obj)
{
  byte newMonitorCount;

#if DEBUG_MONITOR
  printf("exit_monitor of %d\n",(int)obj);
#endif

  if (obj == JNULL)
  {
    // Exiting due to a NPE on monitor_enter [FIX THIS]
    return;
  }

  #ifdef VERIFY
  assert (get_thread_id(obj) == currentThread->threadId, THREADS7);
  assert (get_monitor_count(obj) > 0, THREADS8);
  #endif

  newMonitorCount = get_monitor_count(obj)-1;
  if (newMonitorCount == 0)
    set_thread_id (obj, NO_OWNER);
  set_monitor_count (obj, newMonitorCount);
}

/**
 * Current thread waits for thread to die.
 *
 * throws InterruptedException
 */
void join_thread(Thread *thread)
{
}

void dequeue_thread(Thread *thread)
{
  // First take it out of its current queue
  int cIndex = thread->priority-1;
  Thread **pThreadQ = &threadQ[cIndex];
  
  // Find the previous thread at the old priority
  Thread *previous = *pThreadQ;
  #if DEBUG_THREADS
  printf("Previous thread %ld\n", ptr2word(previous));
  #endif
  while (word2ptr(previous->nextThread) != thread)
    previous = word2ptr(previous->nextThread);

  #if DEBUG_THREADS
  printf("Previous thread %ld\n", ptr2word(previous));
  #endif
  if (previous == thread)
  {
  #if DEBUG_THREADS
  printf("No more threads of priority %d\n", thread->priority);
  #endif
    *pThreadQ = null;
  }
  else
  {
    previous->nextThread = thread->nextThread;
    *pThreadQ = previous;
  }  
}

void enqueue_thread(Thread *thread)
{
  // Could insert it anywhere. Just insert it at the end.
  int cIndex = thread->priority-1;
  Thread *previous = threadQ[cIndex];
  threadQ[cIndex] = thread;
  if (previous == null)
    thread->nextThread = ptr2word(thread);
  else {
    Thread *pNext = word2ptr(previous->nextThread);
    thread->nextThread = ptr2word(pNext);
    previous->nextThread = ptr2word(thread);
  }
}

/**
 * Set the priority of the passed thread. Insert into new queue, remove
 * from old queue. Overload to remove from all queues if passed priority
 * is zero.
 *
 * Returns the 'previous' thread.
 */
void set_thread_priority(Thread *thread, const FOURBYTES priority)
{
  #if DEBUG_THREADS
  printf("Thread priority set to %ld was %d\n", priority, thread->priority);
  #endif
  if (thread->priority == priority)
    return;

  if (thread->state == NEW)
  {
  	// Not fully initialized
  	thread->priority = priority;
  	return;
  }

  dequeue_thread(thread);
  thread->priority = priority;
  enqueue_thread(thread);      
}

