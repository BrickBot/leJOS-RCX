/**
 * This is included inside a switch statement.
 */

case OP_I2F:
  // Arguments: 0
  // Stack: -1 +1
  stackTop[0] = jfloat2word ((float) word2jint(stackTop[0]));
  goto LABEL_ENGINELOOP;
case OP_I2D:
  // Arguments: 0
  // Stack: -1 +2
  stackTop[1] = jfloat2word ((float) word2jint(stackTop[0]));
  stackTop++;
  goto LABEL_ENGINELOOP;
case OP_F2I:
  // Arguments: 0
  // Stack: -1 +1
  stackTop[0] = (int) word2jfloat(stackTop[0]);
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
  stackTop[0] = (int) word2jfloat(stackTop[1]);
  goto LABEL_ENGINELOOP;
case OP_D2F:
  // Arguments: 0
  // Stack: -2 +1
  stackTop--;
  stackTop[0] = stackTop[1];
  goto LABEL_ENGINELOOP;
case OP_I2B:
  stackTop[0] = (JBYTE) word2jint(stackTop[0]);
  goto LABEL_ENGINELOOP;
case OP_I2C:
  stackTop[0] = (JCHAR) word2jint(stackTop[0]);
  goto LABEL_ENGINELOOP; 
case OP_I2S:
  stackTop[0] = (JSHORT) word2jint(stackTop[0]);
  goto LABEL_ENGINELOOP;   

// Notes:
// - Not supported: I2L, L2I, L2F, L2D, F2L, D2L

/*end*/

