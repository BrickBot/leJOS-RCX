/**
 * This is included inside a switch statement.
 */

case OP_NEW:
  // Stack: +1
  // Arguments: 2
  // Hi byte unused
  gStackWord = (STACKWORD) new_object_checked (pc[1], pc - 1);
  if (gMustExit)
    return;
  if (!gStackWord)
  {
    *(++stackTop) = gStackWord;
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_GETSTATIC:
case OP_PUTSTATIC:
case OP_GETFIELD:
case OP_PUTFIELD:
  // Stack: (see method)
  // Arguments: 2
  gByte = *(pc-1);
  handle_field (pc[0], pc[1], (gByte & 0x01), (gByte < OP_GETFIELD));
  if (gMustExit)
    return;
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_INSTANCEOF:
  // Stack: unchanged
  // Arguments: 2
  // Ignore hi byte
  *stackTop = instance_of (mk_pobject (*stackTop),  pc[1]);
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_CHECKCAST:
  // Stack: -1
  // Arguments: 2
  // Ignore hi byte
  pc++;
  if (!instance_of (mk_pobject (*stackTop--), *pc++))
  {
    throw_exception (classCastException);
    if (gMustExit)
      return;
  }
  goto LABEL_ENGINELOOP;

/*end*/








