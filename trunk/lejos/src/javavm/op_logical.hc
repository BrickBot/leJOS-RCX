/**
 * This is included inside a switch statement.
 */

case OP_ISHL:
  // Arguments: 0
  // Stack: -2 +1
  stackTop--;
  *stackTop = word2jint(*stackTop) << (*(stackTop+1) & 0x1F);
  goto LABEL_ENGINELOOP;
case OP_ISHR:
  stackTop--;
  *stackTop = word2jint(*stackTop) >> (*(stackTop+1) & 0x1F);
  goto LABEL_ENGINELOOP;
case OP_IUSHR:
  stackTop--;
  *stackTop = *stackTop >> (*(stackTop+1) & 0x1F);
  goto LABEL_ENGINELOOP;
case OP_IAND:
  stackTop--;
  *stackTop = *stackTop & *(stackTop+1);
  goto LABEL_ENGINELOOP;
case OP_IOR:
  stackTop--;
  *stackTop = *stackTop | *(stackTop+1);
  goto LABEL_ENGINELOOP;
case OP_IXOR:
  stackTop--;
  *stackTop = *stackTop ^ *(stackTop+1);
  goto LABEL_ENGINELOOP;

// Notes:
// - Not supported: LSHL, LSHR, LAND, LOR, LXOR

/*end*/







