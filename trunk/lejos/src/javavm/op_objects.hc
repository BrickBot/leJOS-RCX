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
    push_ref (ptr2ref(gBytePtr));
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_GETSTATIC:
case OP_PUTSTATIC:
  // Stack: +1 or +2 for GETSTATIC, -1 or -2 for PUTSTATIC
  {
    STATICFIELD fieldRecord;
    byte *fieldBase;
    byte fieldType;
    byte fieldSize;
    boolean wideWord;
    boolean isRef;

    #if DEBUG_FIELDS
    printf ("  OP_GET/PUTSTATIC: %d, %d\n", (int) pc[0], (int) pc[1]);
    #endif

    if (dispatch_static_initializer (get_class_record (pc[0]), pc - 1))
      goto LABEL_ENGINELOOP;
    fieldRecord = ((STATICFIELD *) get_static_fields_base())[pc[1]];
    fieldType = (fieldRecord >> 12) & 0x0F;
    isRef = (fieldType == T_REFERENCE);
    fieldSize = typeSize[fieldType];
    wideWord = (fieldSize > 4);
    if (wideWord)
      fieldSize = 4;
    fieldBase = get_static_state_base() + get_static_field_offset (fieldRecord);

    #if DEBUG_FIELDS
    printf ("-- fieldSize  = %d\n", (int) fieldSize);
    printf ("-- fieldBase  = %d\n", (int) fieldBase);
    #endif

    if (*(pc-1) == OP_GETSTATIC)
    {
      make_word (fieldBase, fieldSize, &tempStackWord);
      push_word_or_ref (tempStackWord, isRef);
      if (wideWord)
      {
        make_word (fieldBase + 4, 4, &tempStackWord);
        push_word (tempStackWord);
      }
    }
    else
    {
      if (wideWord)
        store_word (fieldBase + 4, 4, pop_word());
      store_word (fieldBase, fieldSize, pop_word_or_ref (isRef));
    }
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_GETFIELD:
  {
    byte *fieldBase;
    byte fieldType;
    byte fieldSize;
    boolean wideWord;

    tempStackWord = get_top_ref();
    if (tempStackWord == JNULL)
    {
      throw_exception (nullPointerException);
      goto LABEL_ENGINELOOP;
    }
    fieldType = pc[0] >> 12;
    fieldSize = typeSize[fieldType];
    wideWord = (fieldSize > 4);
    if (wideWord)
      fieldSize = 4;
    fieldBase = ((byte *) word2ptr (tempStackWord)) + 
                (((TWOBYTES) (pc[0] & F_OFFSET_MASK) << 8) | pc[1]);
    make_word (fieldBase, fieldSize, &tempStackWord);
    set_top_word_or_ref (tempStackWord, (fieldType == T_REFERENCE));
    if (wideWord)
    {
      make_word (fieldBase + 4, 4, &tempStackWord);
      push_word (tempStackWord);
    }
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_PUTFIELD:
  {
    byte *fieldBase;
    byte fieldType;
    byte fieldSize;
    boolean wideWord;

    fieldType = pc[0] >> 12;
    fieldSize = typeSize[fieldType];
    wideWord = (fieldSize > 4);
    if (wideWord)
      fieldSize = 4;
    tempStackWord = get_ref_at (wideWord ? 2 : 1);
    if (tempStackWord == JNULL)
    {
      throw_exception (nullPointerException);
      goto LABEL_ENGINELOOP;
    }
    fieldBase = ((byte *) word2ptr (tempStackWord)) + 
                (((TWOBYTES) (pc[0] & F_OFFSET_MASK) << 8) | pc[1]);
    if (wideWord)
      store_word (fieldBase + 4, 4, pop_word());
    store_word (fieldBase, fieldSize, pop_word_or_ref (fieldType == T_REFERENCE));
    just_pop_ref();
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_INSTANCEOF:
  // Stack: unchanged
  // Arguments: 2
  // Ignore hi byte
  set_top_word (instance_of (word2obj (get_top_ref()),  pc[1]));
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_CHECKCAST:
  // Stack: -1 +1 (same)
  // Arguments: 2
  // Ignore hi byte
  pc++;
  tempStackWord = get_top_ref();
  if (tempStackWord != JNULL && !instance_of (word2obj (tempStackWord), pc[0]))
    throw_exception (classCastException);
  pc++;
  goto LABEL_ENGINELOOP;

// Notes:
// - NEW, INSTANCEOF, CHECKCAST: 8 bits ignored, 8-bit class index
// - GETSTATIC and PUTSTATIC: 8-bit class index, 8-bit static field record
// - GETFIELD and PUTFIELD: 4-bit field type, 12-bit field data offset

/*end*/








