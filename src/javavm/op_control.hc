/**
 * This is included inside a switch statement.
 */

case OP_IF_ICMPEQ:
case OP_IF_ACMPEQ:
  // Arguments: 2
  // Stack: -2
  do_isub();
  // Fall through!
case OP_IFEQ:
case OP_IFNULL:
  // Arguments: 2
  // Stack: -1
  if (*stackTop-- == 0)
    do_goto();  
  goto LABEL_ENGINELOOP;
case OP_IF_ICMPNE:
case OP_IF_ACMPNE:
  do_isub();
  // Fall through!
case OP_IFNE:
case OP_IFNONNULL:
  if (*stackTop-- != 0)
    do_goto();
  goto LABEL_ENGINELOOP;
case OP_IF_ICMPLT:
  do_isub();
  // Fall through!
case OP_IFLT:
  if (word2jint(*stackTop--) < 0)
    do_goto();
  goto LABEL_ENGINELOOP;
case OP_IF_ICMPLE:
  do_isub();
  // Fall through!
case OP_IFLE:
  if (word2jint(*stackTop--) <= 0)
    do_goto();
  goto LABEL_ENGINELOOP;
case OP_IF_ICMPGE:
  do_isub();
  // Fall through!
case OP_IFGE:
  if (word2jint(*stackTop--) >= 0)
    do_goto();
  goto LABEL_ENGINELOOP;
case OP_IF_ICMPGT:
  do_isub();
  // Fall through!
case OP_IFGT:
  if (word2jint(*stackTop--) > 0)
    do_goto();
  goto LABEL_ENGINELOOP;


case OP_FCMPL:
case OP_FCMPG:
  // TBD: no distinction between opcodes
  stackTop -= 2;
  do_fcmp (word2jfloat(stackTop[1]), word2jfloat(stackTop[2]));
  goto LABEL_ENGINELOOP;
case OP_DCMPL:
case OP_DCMPG:
  stackTop -= 4;
  do_fcmp (word2jfloat(stackTop[2]), word2jfloat(stackTop[4]));
  goto LABEL_ENGINELOOP;


case OP_JSR:
  // Arguments: 2
  // Stack: +1
  *(++stackTop) = ptr2word (pc + 2);
  // Fall through!
case OP_GOTO:
  // Arguments: 2
  // Stack: +0
  do_goto();
  // No pc increment!
  goto LABEL_ENGINELOOP;
case OP_RET:
  // Arguments: 1
  // Stack: +0
  pc = word2ptr (localsBase[pc[0]]);
  // No pc increment!
  goto LABEL_ENGINELOOP;

#if 0

case OP_LOOKUPSWITCH:
  gWord = 4 - (pc - current_code_base()) % 4; 
  if (gWord == 4)
    gWord = 0;
  pc += gWord;
  
  goto LABEL_ENGINELOOP;

#endif 0

// Notes:
// - Not supported: TABLESWITCH, LOOKUPSWITCH, GOTO_W, JSR_W, LCMP

/*end*/







