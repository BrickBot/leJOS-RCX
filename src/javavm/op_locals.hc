/**
 * This is included inside a switch statement.
 */

case OP_ILOAD:
case OP_FLOAD:
case OP_ALOAD:
  // Arguments: 1
  // Stack: +1
  *(++stackTop) = localsBase[*pc++];
  #if DEBUG_BYTECODE
  printf ("  OP_ALOAD pushed: %d\n", (int) stackTop[0]);
  #endif
  goto LABEL_ENGINELOOP;
case OP_ILOAD_0:
case OP_ILOAD_1:
case OP_ILOAD_2:
case OP_ILOAD_3:
  // Arguments: 0
  // Stack: +1
  *(++stackTop) = localsBase[*(pc-1)-OP_ILOAD_0];
  goto LABEL_ENGINELOOP;
case OP_FLOAD_0:
case OP_FLOAD_1:
case OP_FLOAD_2:
case OP_FLOAD_3:
  // Arguments: 0
  // Stack: +1
  *(++stackTop) = localsBase[*(pc-1)-OP_FLOAD_0];
  goto LABEL_ENGINELOOP;
case OP_ALOAD_0:
case OP_ALOAD_1:
case OP_ALOAD_2:
case OP_ALOAD_3:
  // Arguments: 0
  // Stack: +1
  *(++stackTop) = localsBase[*(pc-1)-OP_ALOAD_0];
  #if DEBUG_BYTECODE
  printf ("  OP_ALOAD_<N> pushed: %d\n", (int) stackTop[0]);
  #endif
  goto LABEL_ENGINELOOP;
case OP_LLOAD:
case OP_DLOAD:
  // Arguments: 1
  // Stack: +2
  *(++stackTop) = localsBase[*pc];
  *(++stackTop) = localsBase[(*pc)+1];
  pc++;
  goto LABEL_ENGINELOOP;
case OP_LLOAD_0:
case OP_LLOAD_1:
case OP_LLOAD_2:
case OP_LLOAD_3:
  // Arguments: 0
  // Stack: +2
  gByte = *(pc-1) - OP_LLOAD_0;
  *(++stackTop) = localsBase[gByte++];
  *(++stackTop) = localsBase[gByte];
  goto LABEL_ENGINELOOP;
case OP_DLOAD_0:
case OP_DLOAD_1:
case OP_DLOAD_2:
case OP_DLOAD_3:
  // Arguments: 0
  // Stack: +2
  gByte = *(pc-1) - OP_DLOAD_0;
  *(++stackTop) = localsBase[gByte++];
  *(++stackTop) = localsBase[gByte];
  goto LABEL_ENGINELOOP;
case OP_ISTORE:
case OP_FSTORE:
case OP_ASTORE:
  // Arguments: 1
  // Stack: -1
  localsBase[*pc++] = *stackTop--;
  goto LABEL_ENGINELOOP;
case OP_ISTORE_0:
case OP_ISTORE_1:
case OP_ISTORE_2:
case OP_ISTORE_3:
  // Arguments: 0
  // Stack: -1
  localsBase[*(pc-1)-OP_ISTORE_0] = *stackTop--;
  goto LABEL_ENGINELOOP;
case OP_FSTORE_0:
case OP_FSTORE_1:
case OP_FSTORE_2:
case OP_FSTORE_3:
  // Arguments: 0
  // Stack: -1
  localsBase[*(pc-1)-OP_FSTORE_0] = *stackTop--;
  goto LABEL_ENGINELOOP;
case OP_ASTORE_0:
case OP_ASTORE_1:
case OP_ASTORE_2:
case OP_ASTORE_3:
  // Arguments: 0
  // Stack: -1
  localsBase[*(pc-1)-OP_ASTORE_0] = *stackTop--;
  goto LABEL_ENGINELOOP;
case OP_LSTORE:
case OP_DSTORE:
  // Arguments: 1
  // Stack: -1
  localsBase[(*pc)+1] = *stackTop--;
  localsBase[*pc] = *stackTop--;
  pc++;
  goto LABEL_ENGINELOOP;
case OP_LSTORE_0:
case OP_LSTORE_1:
case OP_LSTORE_2:
case OP_LSTORE_3:
  gByte = *(pc-1) - OP_LSTORE_0;
  localsBase[gByte+1] = *stackTop--;
  localsBase[gByte] = *stackTop--;
  goto LABEL_ENGINELOOP;
case OP_DSTORE_0:
case OP_DSTORE_1:
case OP_DSTORE_2:
case OP_DSTORE_3:
  gByte = *(pc-1) - OP_DSTORE_0;
  localsBase[gByte+1] = *stackTop--;
  localsBase[gByte] = *stackTop--;
  goto LABEL_ENGINELOOP;
case OP_IINC:
  // Arguments: 2
  // Stack: +0
  localsBase[*pc] += byte2jint(*(pc+1));
  pc += 2; 
  goto LABEL_ENGINELOOP;

// Notes:
// - OP_WIDE is unexpected in TinyVM.

/*end*/







