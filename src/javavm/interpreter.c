
#include "trace.h"
#include "types.h"
#include "constants.h"
#include "classes.h"
#include "threads.h"
#include "opcodes.h"
#include "configure.h"

// Interpreter globals:

boolean gMustExit;
byte *pc = null;
STACKWORD *localsBase = null;
STACKWORD *stackTop = null;

// Temporary globals:

byte gByte;
ConstantRecord *gConstRec;
STACKWORD gStackWord;

/* byte *current_code_base() */
/* { */
/*   return get_binary_base() + current_method()->codeOffset; */
/* } */

/**
 * Assumes pc points to 2-byte offset, and jumps.
 */
void do_goto()
{
  pc += (((JSHORT) pc[0] << 8) | pc[1]) - 1;
}

void do_isub()
{
  stackTop--;
  stackTop[0] = word2jint(stackTop[0]) - word2jint(stackTop[1]);
}

void do_fcmp (JFLOAT f1, JFLOAT f2)
{
  // TBD: NaN

  stackTop++;
  if (f1 > f2)
    stackTop[0] = 1;
  else if (f1 == f2)
    stackTop[0] = 0;
  else if (f1 < f2)
    stackTop[0] = -1;
}

/**
 * Everything runs inside here, essentially.
 * Notes:
 * 1. currentThread must be initialized.
 */
void engine()
{
  register short numOpcodes;

  #ifdef VERIFY
  assert (currentThread != null, INTERPRETER1);
  #endif

  gMustExit = false;
  switch_thread();
  #ifdef VERIFY
  assert (gMustExit == false, INTERPRETER2);
  #endif VERIFY
  numOpcodes = 1;
 LABEL_ENGINELOOP: 
  if (!(--numOpcodes))
  {
    switch_thread();
    numOpcodes = OPCODES_PER_TIME_SLICE;
  }

  //-----------------------------------------------
  // SWITCH BEGINS HERE
  //-----------------------------------------------

  switch (*pc++)
  {

  }

  //-----------------------------------------------
  // SWITCH ENDS HERE
  //-----------------------------------------------

  #ifdef VERIFY
  assert (false, 1000 + *pc);
  #endif VERIFY
}




