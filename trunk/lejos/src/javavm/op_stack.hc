/**
 * This is included inside a switch statement.
 */

case OP_BIPUSH:
  // Stack size: +1
  // Arguments: 1
  // TBD: check negatives
  *(++stackTop) = (STACKWORD) (char) (*pc++);
  goto LABEL_ENGINELOOP;
case OP_SIPUSH:
  // Stack size: +1
  // Arguments: 2
  #if DEBUG_BYTECODE
  printf ("  OP_SIPUSH args: %d, %d (%d)\n", (int) pc[0], (int) pc[1], (int) pc[2]);
  #endif
  *(++stackTop) = (STACKWORD) (short) (((TWOBYTES) pc[0] << 8) | pc[1]);
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_LDC:
  // Stack size: +1
  // Arguments: 1
  gConstRec = get_constant_record (*pc++);

  // TBD: strings

  #ifdef VERIFY
  assert (gConstRec->constantSize <= 4, INTERPRETER5);
  #endif VERIFY

  make_word (get_constant_ptr(gConstRec), 
             gConstRec->constantSize, ++stackTop);
  goto LABEL_ENGINELOOP;
case OP_LDC2_W:
  // Stack size: +2
  // Arguments: 2
  gConstRec = get_constant_record (((TWOBYTES) pc[0] << 8) | pc[1]);

  #ifdef VERIFY
  assert (gConstRec->constantSize == 8, INTERPRETER6);
  #endif VERIFY

  gBytePtr = get_constant_ptr (gConstRec);
  make_word (gBytePtr, sizeof(STACKWORD), ++stackTop);
  make_word (gBytePtr + sizeof(STACKWORD), sizeof(STACKWORD),
             ++stackTop);
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_ACONST_NULL:
  // Stack size: +1
  // Arguments: 0
  *(++stackTop) = JNULL;
  goto LABEL_ENGINELOOP;
case OP_ICONST_M1:
case OP_ICONST_0:
case OP_ICONST_1:
case OP_ICONST_2:
case OP_ICONST_3:
case OP_ICONST_4:
case OP_ICONST_5:
  // Stack size: +1
  // Arguments: 0
  *(++stackTop) = *(pc-1) - OP_ICONST_0;
  goto LABEL_ENGINELOOP;
case OP_LCONST_0:
case OP_LCONST_1:
  // Stack size: +2
  // Arguments: 0
  *(++stackTop) = 0;
  *(++stackTop) = *(pc-1) - OP_LCONST_0;
  goto LABEL_ENGINELOOP;
case OP_DCONST_0:
  *(++stackTop) = 0;
  // Fall through!
case OP_FCONST_0:
  *(++stackTop) = jfloat2word((JFLOAT) 0.0);
  goto LABEL_ENGINELOOP;
case OP_FCONST_1:
  *(++stackTop) = jfloat2word((JFLOAT) 1.0);
  goto LABEL_ENGINELOOP;
case OP_FCONST_2:
  *(++stackTop) = jfloat2word((JFLOAT) 2.0);
  goto LABEL_ENGINELOOP;
case OP_DCONST_1:
  // Stack size: +2
  // Arguments: 0
  *(++stackTop) = 0;
  *(++stackTop) = jfloat2word((JFLOAT) 1.0);
  goto LABEL_ENGINELOOP;
case OP_POP2:
  // Stack size: -2
  // Arguments: 0
  stackTop--;
  // Fall through
case OP_POP:
  // Stack size: -1
  // Arguments: 0
  stackTop--;
  goto LABEL_ENGINELOOP;
case OP_DUP:
  // Stack size: +1
  // Arguments: 0
  *(stackTop+1) = *stackTop;
  stackTop++;
  goto LABEL_ENGINELOOP;
case OP_DUP2:
  // Stack size: +2
  // Arguments: 0
  *(stackTop+1) = *(stackTop-1);
  *(stackTop+2) = *stackTop;
  stackTop += 2;
  goto LABEL_ENGINELOOP;
case OP_DUP_X1:
  // Stack size: +1
  // Arguments: 0
  stackTop++;
  *stackTop = *(stackTop-1);
  *(stackTop-1) = *(stackTop-2);
  *(stackTop-2) = *stackTop;
  goto LABEL_ENGINELOOP;
case OP_DUP2_X1:
  // Stack size: +2
  // Arguments: 0
  stackTop += 2;
  *stackTop = *(stackTop-2);
  *(stackTop-1) = *(stackTop-3);
  *(stackTop-2) = *(stackTop-4);
  *(stackTop-3) = *stackTop;
  *(stackTop-4) = *(stackTop-1);
  goto LABEL_ENGINELOOP;
case OP_DUP_X2:
  // Stack size: +1
  // Arguments: 0
  stackTop++;
  *stackTop = *(stackTop-1);
  *(stackTop-1) = *(stackTop-2);
  *(stackTop-2) = *(stackTop-3);
  *(stackTop-3) = *stackTop;
  goto LABEL_ENGINELOOP;
case OP_DUP2_X2:
  // Stack size: +2
  // Arguments: 0
  stackTop += 2;
  gByte = 4;
  while (gByte--)
  {
    *stackTop = *(stackTop-2);  
    stackTop--;
  }
  stackTop[0] = stackTop[4];
  stackTop[-1] = stackTop[3];
  goto LABEL_ENGINELOOP;
case OP_SWAP:
  gStackWord = *stackTop;
  *stackTop = *(stackTop-1);
  *(stackTop-1) = gStackWord;
  goto LABEL_ENGINELOOP;

// Notes:
// - LDC_W should not occur in tinyvm.
// - Arguments of LDC and LDC2_W are postprocessed.
// - NOOP is in op_skip.hc.

/*end*/







