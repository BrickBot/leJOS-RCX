/**
 * This is included inside a switch statement.
 */

case OP_ISUB:
  // Arguments: 0
  // Stack: -2 +1
  *stackTop = -word2jint(*stackTop);
  // Fall through!
case OP_IADD:
  // Arguments: 0
  // Stack: -2 +1
  stackTop--;
  *stackTop = word2jint(*stackTop) + word2jint(*(stackTop+1));
  goto LABEL_ENGINELOOP;
case OP_IMUL:
  stackTop--;
  *stackTop = word2jint(*stackTop) * word2jint(*(stackTop+1));
  goto LABEL_ENGINELOOP;
case OP_IDIV:
case OP_IREM:
  gInt = word2jint(stackTop[0]);
  if (gInt == 0)
  {
    throw_exception (arithmeticException);
    if (gMustExit)
      return;
    goto LABEL_ENGINELOOP;
  }
  stackTop--;
  stackTop[0] = (*(pc-1) == OP_IDIV) ? word2jint(stackTop[0]) / gInt :
                                       word2jint(stackTop[0]) % gInt;
  goto LABEL_ENGINELOOP;
case OP_INEG:
  *stackTop = -word2jint(*stackTop);
  goto LABEL_ENGINELOOP;


#if FP_ARITHMETIC

case OP_FSUB:
  *stackTop = jfloat2word(-word2jfloat(*stackTop));
  // Fall through!
case OP_FADD:
  *(stackTop-1) = jfloat2word(word2jfloat(*(stackTop-1)) + 
                  word2jfloat(*stackTop));
  stackTop--;
  goto LABEL_ENGINELOOP;
case OP_FMUL:
  *(stackTop-1) = jfloat2word(word2jfloat(*(stackTop-1)) * 
                  word2jfloat(*stackTop));
  stackTop--;
  goto LABEL_ENGINELOOP;
case OP_FDIV:
  *(stackTop-1) = jfloat2word(word2jfloat(*(stackTop-1)) /
                  word2jfloat(*stackTop));
  stackTop--;
  goto LABEL_ENGINELOOP;
case OP_FNEG:
case OP_DNEG:
  *stackTop = jfloat2word(-word2jfloat(*stackTop));
  stackTop--;
  goto LABEL_ENGINELOOP;
case OP_DSUB:
  *stackTop = jfloat2word(-word2jfloat(*stackTop));
  // Fall through!
case OP_DADD:
  *(stackTop-2) = jfloat2word(word2jfloat(*(stackTop-2)) +
                  word2jfloat(*stackTop));
  stackTop -= 2;
  goto LABEL_ENGINELOOP;
case OP_DMUL:
  *(stackTop-2) = jfloat2word(word2jfloat(*(stackTop-2)) *
                  word2jfloat(*stackTop));
  stackTop -= 2;
  goto LABEL_ENGINELOOP;
case OP_DDIV:
  *(stackTop-2) = jfloat2word(word2jfloat(*(stackTop-2)) /
                  word2jfloat(*stackTop));
  stackTop -= 2;
  goto LABEL_ENGINELOOP;

#else // no FP_ARITHMETIC

case OP_FADD:
case OP_FSUB:
case OP_FMUL:
case OP_FDIV:
  stackTop--;
  goto LABEL_ENGINELOOP;
case OP_DADD:
case OP_DSUB:
case OP_DMUL:
case OP_DDIV:
  stackTop -= 2;
  goto LABEL_ENGINELOOP;
case OP_FNEG:
case OP_DNEG:
  goto LABEL_ENGINELOOP;
  
#endif FP_ARITHMETIC

// Notes:
// - Not supported: LADD, LSUB, LMUL, LREM, FREM, DREM
// - Operations on doubles are truncated to low float
// - Floating point operation only supported if FP_ARITHMETIC != 0

/*end*/







