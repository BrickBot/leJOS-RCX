/**
 * This is included inside a switch statement.
 */

// TBD: ArrayIndexOutOfBounds!!

case OP_NEWARRAY:
  // Stack size: unchanged
  // Arguments: 1
  *stackTop = obj2word(new_primitive_array (*pc++, *stackTop));
  if (gMustExit)
    return;
  goto LABEL_ENGINELOOP;
case OP_MULTIANEWARRAY:
  // Stack size: -N + 1
  // Arguments: 3
  *(++stackTop) = obj2word(new_multi_array (pc[0], pc[1], pc[2]));
  if (gMustExit)
    return;
  pc += 3;
  goto LABEL_ENGINELOOP;
case OP_IALOAD:
case OP_FALOAD:
case OP_AALOAD:
  // Stack size: -1
  // Arguments: 0
  stackTop--;
  *stackTop = get_array_word ((REFERENCE) *stackTop, 4, *(stackTop+1));
  if (gMustExit)
    return;
  goto LABEL_ENGINELOOP;
case OP_LALOAD:
case OP_DALOAD:
  // Stack size: -2 + 2
  // Arguments: 0
  gInt = word2jint(*stackTop--) * 2;
  gStackWord = *stackTop;
  *stackTop++ = get_array_word (gStackWord, 4, gInt++);
  *stackTop = get_array_word (gStackWord, 4, gInt);
  if (gMustExit)
    return;
  goto LABEL_ENGINELOOP;
case OP_BALOAD:
case OP_CALOAD:
case OP_SALOAD:
  // Stack size: -1
  // Arguments: 0
  gByte = *(pc-1);
  stackTop--;
  *stackTop = get_array_word (word2obj(*stackTop), 
              (gByte == OP_BALOAD) ? 1 : 2, *(stackTop+1));
  if (gMustExit)
    return;
  goto LABEL_ENGINELOOP;
case OP_IASTORE:
case OP_FASTORE:
case OP_AASTORE:
  // Stack size: -3
  // Arguments: 0
  set_array_word (word2obj(*(stackTop-2)), 4, *(stackTop-1), *stackTop);
  if (gMustExit)
    return;
  stackTop -= 3;
  goto LABEL_ENGINELOOP;
case OP_DASTORE:
case OP_LASTORE:
  // Stack size: -4
  gInt = word2jint(*(stackTop-2)) * 2;
  set_array_word (word2obj (*(stackTop-3)), 4, gInt++, *(stackTop-1));
  set_array_word (word2obj (*(stackTop-3)), 4, gInt, *stackTop);
  if (gMustExit)
    return;
  stackTop -= 4;
  goto LABEL_ENGINELOOP;
case OP_BASTORE:
case OP_CASTORE:
case OP_SASTORE:
  // Stack size: -3
  gByte = *(pc-1);
  set_array_word (word2obj (*(stackTop-2)), (gByte == OP_BASTORE) ? 1 : 2,
                  *(stackTop-1), *stackTop);
  if (gMustExit)
    return;
  stackTop -= 3;
  goto LABEL_ENGINELOOP;
case OP_ARRAYLENGTH:
  // Stack size: -1 + 1
  // Arguments: 0
  *stackTop = get_array_length (word2obj (*stackTop));
  if (gMustExit)
    return;
  goto LABEL_ENGINELOOP;


// Notes:
// * OP_ANEWARRAY is changed to OP_NEWARRAY of data type 0, plus a NOOP.

/*end*/







