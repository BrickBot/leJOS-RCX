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
  {
    byte *fieldBase;
    STATICFIELD fieldRecord;
    byte fieldSize;
    byte numWordsMinus1;

    #if DEBUG_FIELDS
    printf ("  OP_GETSTATIC: %d, %d, %d, %d\n", (int) pc[0], (int) pc[1], 
            (int) doPut, (int) aStatic);
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

    make_word (fieldBase, fieldSize, ++stackTop);
    if (numWordsMinus1)
      make_word (fieldBase + 4, fieldSize, ++stackTop);
    pc += 2;
  }
  goto LABEL_ENGINELOOP;



case OP_GETFIELD:
case OP_PUTFIELD:
case OP_PUTSTATIC:
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
  // Stack: -1 +1 (same)
  // Arguments: 2
  // Ignore hi byte
  pc++;
  if (!instance_of (word2obj (*stackTop), *pc))
    throw_exception (classCastException);
  pc++;
  goto LABEL_ENGINELOOP;

/*end*/








