
#include "trace.h"
#include "types.h"
#include "constants.h"
#include "classes.h"
#include "threads.h"
#include "opcodes.h"
#include "configure.h"
#include "memory.h"
#include "language.h"
#include "exceptions.h"

#define F_SIZE_MASK    0xE0
#define F_SIZE_SHIFT   5
#define F_OFFSET_MASK  0x1F

#if DEBUG_BYTECODE
extern char *OPCODE_NAME[];
#endif

// Interpreter globals:

boolean gMustExit;
byte *pc;
STACKWORD *localsBase;
STACKWORD *stackTop;

// Temporary globals:

byte tempByte;
byte *tempBytePtr;
JFLOAT tempFloat;
ConstantRecord *tempConstRec;
STACKWORD tempStackWord;
STACKWORD *tempWordPtr;
JINT tempInt;

/**
 * Assumes pc points to 2-byte offset, and jumps.
 */
void do_goto (boolean aCond)
{
  #if 0
  printf ("do_goto: %d, %d (= %d)\n", (int) pc[0], (int) pc[1],
          (JSHORT) (((TWOBYTES) pc[0] << 8) | pc[1]));
  #endif

  if (aCond)
  {
    pc += (JSHORT) (((TWOBYTES) pc[0] << 8) | pc[1]);
    pc--;
  }
  else
  {
    pc += 2;
  }
}

void do_isub (void)
{
  stackTop--;
  stackTop[0] = word2jint(stackTop[0]) - word2jint(stackTop[1]);
}

void do_fcmp (JFLOAT f1, JFLOAT f2)
{
  // TBD: NaN

  stackTop++;

  #if FP_ARITHMETIC

  if (f1 > f2)
    stackTop[0] = 1;
  else if (f1 == f2)
    stackTop[0] = 0;
  else if (f1 < f2)
    stackTop[0] = -1;

  #else

  stackTop[0] = 0;
 
  #endif FP_ARITHMETIC
}

/**
 * @return A String instance, or JNULL if an exception was thrown
 *         or the static initializer of String had to be executed.
 */
static inline Object *create_string (ConstantRecord *constantRecord, 
                                     byte *btAddr)
{
  Object *ref;
  Object *arr;
  JINT    i;

  ref = new_object_checked (JAVA_LANG_STRING, btAddr);
  if (ref == JNULL)
    return JNULL;
  arr = new_primitive_array (T_CHAR, constantRecord->constantSize);
  if (arr == JNULL)
  {
    deallocate (ref, class_size (JAVA_LANG_STRING));    
    return JNULL;
  }
  ((String *) ref)->characters = obj2ref(arr);
  for (i = 0; i < constantRecord->constantSize; i++)
    jchararray(arr)[i] = (JCHAR) get_constant_ptr(constantRecord)[i];
}

/**
 * Pops the array index off the stack, assigns
 * both tempInt and tempBytePtr, and checks
 * bounds and null reference.
 */
boolean array_access_helper()
{
  tempInt = word2jint(pop_word());
  tempBytePtr = word2ptr(get_top_word());
  if (tempBytePtr == JNULL)
    throw_exception (nullPointerException);
  else if (tempInt < 0 || tempInt >= get_array_length ((Object *) tempBytePtr))
    throw_exception (arrayIndexOutOfBoundsException);
  else
    return true;
  return false;
}

/**
 * Everything runs inside here, essentially.
 * Notes:
 * 1. currentThread must be initialized.
 */
void engine()
{
  register short numOpcodes;
  
  gMustExit = false;
  switch_thread();
  numOpcodes = OPCODES_PER_TIME_SLICE;
 LABEL_ENGINELOOP: 
  if (gMustExit)
    return;
  if (!(--numOpcodes))
  {
    #if DEBUG_THREADS
    printf ("switching threads: %d\n", (int) numOpcodes);
    #endif
    switch_thread();
    numOpcodes = OPCODES_PER_TIME_SLICE;
  }

  //-----------------------------------------------
  // SWITCH BEGINS HERE
  //-----------------------------------------------

  #ifdef DEBUG_BYTECODE
  printf ("OPCODE: (0x%X) %s\n", (int) *pc, OPCODE_NAME[*pc]);
  #endif

  switch (*pc++)
  {
    #include "op_stack.hc"
    #include "op_locals.hc"
    #include "op_arrays.hc"
    #include "op_objects.hc"
    #include "op_control.hc"
    #include "op_methods.hc"
    #include "op_other.hc"
    #include "op_skip.hc"
    #include "op_conversions.hc"
    #include "op_logical.hc"
    #include "op_arithmetic.hc"
  }

  //-----------------------------------------------
  // SWITCH ENDS HERE
  //-----------------------------------------------

  // This point should never be reached

  #ifdef VERIFY
  assert (false, 1000 + *pc);
  #endif VERIFY
}




