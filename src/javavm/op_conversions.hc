/**
 * This is included inside a switch statement.
 */

case OP_I2B:
  stackTop[0] = (JBYTE) word2jint(stackTop[0]);
  goto LABEL_ENGINELOOP;
case OP_I2S:
case OP_I2C:
  stackTop[0] = (JSHORT) word2jint(stackTop[0]);
  goto LABEL_ENGINELOOP;   

#if FP_ARITHMETIC

case OP_I2F:
  // Arguments: 0
  // Stack: -1 +1
  stackTop[0] = jfloat2word ((JFLOAT) word2jint(stackTop[0]));
  goto LABEL_ENGINELOOP;
case OP_I2D:
  // Arguments: 0
  // Stack: -1 +2
  stackTop[1] = jfloat2word ((JFLOAT) word2jint(stackTop[0]));
  stackTop++;
  goto LABEL_ENGINELOOP;
case OP_F2I:
  // Arguments: 0
  // Stack: -1 +1
  stackTop[0] = (JINT) word2jfloat(stackTop[0]);
  goto LABEL_ENGINELOOP;
case OP_F2D:
  // Arguments: 0
  // Stack: -1 +2
  stackTop[1] = stackTop[0];
  stackTop++;
  goto LABEL_ENGINELOOP;
case OP_D2I:
  // Arguments: 0
  // Stack: -2 +1
  stackTop--;
  stackTop[0] = (JINT) word2jfloat(stackTop[1]);
  goto LABEL_ENGINELOOP;
case OP_D2F:
  // Arguments: 0
  // Stack: -2 +1
  stackTop--;
  stackTop[0] = stackTop[1];
  goto LABEL_ENGINELOOP;

#else // no FP_ARITHMETIC

case OP_I2D:
case OP_F2D:
  // Arguments: 0
  // Stack: -1 +2
  stackTop++;
  // Fall through!
case OP_I2F:
case OP_F2I:
  // Arguments: 0
  // Stack: -1 +1
  goto LABEL_ENGINELOOP;
case OP_D2I:
case OP_D2F:
  // Arguments: 0
  // Stack: -2 +1
  stackTop--;
  goto LABEL_ENGINELOOP;

#endif FP_ARITHMETIC

// Notes:
// - Not supported: I2L, L2I, L2F, L2D, F2L, D2L

/*end*/

