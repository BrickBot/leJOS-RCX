/**
 * This is included inside a switch statement.
 */

case OP_NEWARRAY:
  // Stack size: unchanged
  // Arguments: 1
  *stackTop = new_primitive_array (*pc++, *stackTop);
  goto LABEL_ENGINELOOP;
case OP_MULTIANEWARRAY:
  // Stack size: -N + 1
  // Arguments: 3
  // Skip first byte
  *(++stackTop) = new_multi_array (pc[1], pc[2]);
  pc += 3;
  goto LABEL_ENGINELOOP;
case OP_IALOAD:
case OP_FALOAD:
case OP_AALOAD:
  // Stack size: -1
  // Arguments: 0
  stackTop--;
  *stackTop = get_array_element ((REFERENCE) *stackTop, 4, *(stackTop+1));
  goto LABEL_ENGINELOOP;
case OP_LALOAD:
case OP_DALOAD:
  // Stack size: -2 + 2
  // Arguments: 0
  wrd1 = (*stackTop) * 2;
  stackTop--;
  ref1 = (REFERENCE) *stackTop;
  *stackTop++ = get_array_element (ref1, 4, wrd1++);
  *stackTop = get_array_element (ref1, 4, wrd1);
  goto LABEL_ENGINELOOP;
case OP_BALOAD:
case OP_CALOAD:
case OP_SALOAD:
  // Stack size: -1
  // Arguments: 0
  aux1 = *(pc-1);
  stackTop--;
  *stackTop = get_array_element ((REFERENCE) *stackTop, 
              (aux1 == OP_BALOAD) ? 1 : 2, *(stackTop+1));
  goto LABEL_ENGINELOOP;
case OP_IASTORE:
case OP_FASTORE:
case OP_AASTORE:
  // Stack size: -3
  // Arguments: 0
  set_array_element ((REFERENCE) *(stackTop-2), 4, *(stackTop-1), *stackTop);
  stackTop -= 3;
  goto LABEL_ENGINELOOP;
case OP_DASTORE:
case OP_LASTORE:
  // Stack size: -4
  wrd1 = (*(stackTop-2)) * 2;
  set_array_element ((REFERENCE) *(stackTop-3), 4, wrd1++, *(stackTop-1));
  set_array_element ((REFERENCE) *(stackTop-3), 4, wrd1, *stackTop);
  stackTop -= 4;
  goto LABEL_ENGINELOOP;
case OP_BASTORE:
case OP_CASTORE:
case OP_SASTORE:
  // Stack size: -3
  aux1 = *(pc-1);
  set_array_element ((REFERENCE) *(stackTop-2), (aux == OP_BASTORE) ? 1 : 2,
                     *(stackTop-1), *stackTop);
  stackTop -= 3;
  goto LABEL_ENGINELOOP;
case OP_ARRAYLENGTH:
  // Stack size: -1 + 1
  // Arguments: 0
  *stackTop = get_array_length ((REFERENCE) *stackTop);
  goto LABEL_ENGINELOOP;


// Notes:
// * OP_ANEWARRAY is changed to OP_NEWARRAY of data type 0, plus a NOOP.

/*end*/







