/**
 * This is included inside a switch statement.
 */

case OP_NEW:
  // Stack: +1
  // Arguments: 2
  // Hi byte unused
  gBytePtr = (byte *) new_object_checked (pc[1], pc - 1);
  if (gBytePtr != JNULL)
  { 
    #if 0
    trace (-1, (short) pc[1], 1);
    trace (-1, (short) gBytePtr, 2);
    trace (-1, get_class_index((Object *) gBytePtr), 3);
    #endif
    *(++stackTop) = ptr2word(gBytePtr);
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_GETSTATIC:
case OP_PUTSTATIC:
case OP_GETFIELD:
case OP_PUTFIELD:
  // Stack: (see method)
  // Arguments: 2
  pc--;
  handle_field (pc[1], pc[2], (pc[0] & 0x01), (pc[0] < OP_GETFIELD), pc);
  pc += 3;
  goto LABEL_ENGINELOOP;
case OP_INSTANCEOF:
  // Stack: unchanged
  // Arguments: 2
  // Ignore hi byte
  *stackTop = instance_of (word2obj (*stackTop),  pc[1]);
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_CHECKCAST:
  // Stack: -1
  // Arguments: 2
  // Ignore hi byte
  pc++;
  if (!instance_of (word2obj (*stackTop--), *pc++))
    throw_exception (classCastException);
  goto LABEL_ENGINELOOP;

/*end*/








