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


// Temporary hack here:
// (Work-around for bug)

case OP_GETSTATIC:
case OP_PUTSTATIC:
  // Stack: +1 or +2 for GETSTATIC, -1 or -2 for PUTSTATIC
  {
    byte *fieldBase;
    STATICFIELD fieldRecord;
    byte fieldSize;
    byte numWordsMinus1;

    #if DEBUG_FIELDS
    printf ("  OP_GET/PUTSTATIC: %d, %d\n", (int) pc[0], (int) pc[1]);
    #endif

    if (dispatch_static_initializer (get_class_record (pc[0]), pc - 1))
      goto LABEL_ENGINELOOP;
    fieldRecord = ((STATICFIELD *) get_static_fields_base())[pc[1]];
    fieldSize = ((fieldRecord >> 12) & 0x03) + 1;
    numWordsMinus1 = fieldRecord >> 14;
    fieldBase = get_static_state_base() + get_static_field_offset (fieldRecord);

    #if DEBUG_FIELDS
    printf ("-- fieldSize  = %d\n", (int) fieldSize);
    printf ("-- fieldBase  = %d\n", (int) fieldBase);
    #endif

    if (*(pc-1) == OP_GETSTATIC)
    {
      make_word (fieldBase, fieldSize, ++stackTop);
      if (numWordsMinus1)
        make_word (fieldBase + 4, 4, ++stackTop);
    }
    else
    {
      if (numWordsMinus1)
        save_word (fieldBase + 4, 4, *stackTop--);
      save_word (fieldBase, fieldSize, *stackTop--);
    }
    pc += 2;
  }
  goto LABEL_ENGINELOOP;



case OP_GETFIELD:
case OP_PUTFIELD:
  // Arguments: 2
  {
    byte *fieldBase;
    byte fieldSize;
    byte numWordsMinus1;
    boolean doPut;

    #if DEBUG_FIELDS
    printf ("OP_GET/PUTFIELD: %d, %d\n", (int) pc[0], (int) pc[1]);
    #endif

    doPut = (*(pc-1) == OP_PUTFIELD);
    fieldSize = ((pc[0] >> F_SIZE_SHIFT) & 0x03) + 1;
    numWordsMinus1 = pc[0] >> 7;
    if (doPut)
      stackTop -= (numWordsMinus1 + 1);

    #if DEBUG_FIELDS
    printf ("-- numWords-1    = %d\n", (int) numWordsMinus1);
    printf ("-- stackTop[0,1] = %d, %d\n", (int) stackTop[0], (int) stackTop[1]);
    #endif

    if (stackTop[0] == JNULL)
    {
      throw_exception (nullPointerException);
      goto LABEL_ENGINELOOP;
    }
    fieldBase = ((byte *) word2ptr (stackTop[0])) + 
                (((TWOBYTES) (pc[0] & F_OFFSET_MASK) << 8) | pc[1]);
    if (doPut)
      stackTop++;

    #if DEBUG_FIELDS
    printf ("-- fieldSize  = %d\n", (int) fieldSize);
    printf ("-- fieldBase  = %d\n", (int) fieldBase);
    #endif

    if (doPut)
    {
      if (numWordsMinus1)
        save_word (fieldBase + 4, 4, stackTop[1]);
      save_word (fieldBase, fieldSize, stackTop[0]);
      stackTop -= 2;
    }
    else
    {
      make_word (fieldBase, fieldSize, stackTop);
      if (numWordsMinus1)
        make_word (fieldBase + 4, 4, ++stackTop);
    }
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_INSTANCEOF:
  // Stack: unchanged
  // Arguments: 2
  // Ignore hi byte
  *stackTop = instance_of (word2obj (*stackTop),  pc[1]);
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_CHECKCAST:
  // Stack: -1 +1 (same)
  // Arguments: 2
  // Ignore hi byte
  pc++;
  if (*stackTop != JNULL && !instance_of (word2obj (*stackTop), *pc))
    throw_exception (classCastException);
  pc++;
  goto LABEL_ENGINELOOP;

/*end*/








