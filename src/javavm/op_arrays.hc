/**
 * This is included inside a switch statement.
 */

case OP_NEWARRAY:
  // Stack size: unchanged
  // Arguments: 1
  set_top_ref (obj2ref(new_primitive_array (*pc++, get_top_word())));
  // Exceptions are taken care of
  goto LABEL_ENGINELOOP;
case OP_MULTIANEWARRAY:
  // Stack size: -N + 1
  // Arguments: 3
  tempBytePtr = (byte *) new_multi_array (pc[0], pc[1], pc[2], get_stack_ptr());
  pop_words (pc[2] - 1);
  set_top_ref (ptr2ref (tempBytePtr));
  pc += 3;
  goto LABEL_ENGINELOOP;

-----------------

case OP_AALOAD:
  // Stack size: -2 + 1
  // Arguments: 0
  if (!array_access_helper())
    goto LABEL_ENGINE_LOOP;
  // tempBytePtr and tempInt set by call above
  set_top_ref (word_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_IALOAD:
case OP_FALOAD:
  // Stack size: -2 + 1
  // Arguments: 0
  if (!array_access_helper())
    goto LABEL_ENGINE_LOOP;
  set_top_word (word_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_CALOAD:
case OP_SALOAD:
  if (!array_access_helper())
    goto LABEL_ENGINE_LOOP;
  set_top_word (jshort_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_BALOAD:
  if (!array_access_helper())
    goto LABEL_ENGINE_LOOP;
  set_top_word (jbyte_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_LALOAD:
case OP_DALOAD:
  // Stack size: -2 + 1
  // Arguments: 0
  if (!array_access_helper())
    goto LABEL_ENGINE_LOOP;
  tempInt *= 2;
  set_top_word (word_array(tempBytePtr)[tempInt++]);
  push_word (word_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;

----------------------------

case OP_IASTORE:
case OP_FASTORE:
case OP_AASTORE:
case OP_DASTORE:
case OP_LASTORE:
case OP_BASTORE:
case OP_CASTORE:
case OP_SASTORE:
  // Stack size: -3
  gByte = *(pc-1);
  if (gByte == OP_DASTORE || gByte == OP_LASTORE)
    stackTop -= 2;
  else
    stackTop--;
  gInt = word2jint(stackTop[0]);
  stackTop--;
  gBytePtr = word2ptr(stackTop[0]);
  stackTop--;

  if (gBytePtr == JNULL)
    throw_exception (nullPointerException);
  else
  { 
    if (gInt < 0 || gInt >= get_array_length ((Object *) gBytePtr))
      throw_exception (arrayIndexOutOfBoundsException);
    else
    {
       switch (gByte)
       {
         case OP_IASTORE:
         case OP_AASTORE:
         case OP_FASTORE:
           word_array(gBytePtr)[gInt] = stackTop[3];
           break;
         case OP_SASTORE:
         case OP_CASTORE:
           jshort_array(gBytePtr)[gInt] = stackTop[3];
           break;
         case OP_BASTORE:
           jbyte_array(gBytePtr)[gInt] = stackTop[3];
           break;
         case OP_DASTORE:
         case OP_LASTORE:
           gInt *= 2;
           word_array(gBytePtr)[gInt++] = stackTop[3];
           word_array(gBytePtr)[gInt] = stackTop[4];
           break;
       }  
    }
  }
  goto LABEL_ENGINELOOP;
case OP_ARRAYLENGTH:
  // Stack size: -1 + 1
  // Arguments: 0
  // TBD: Check for NPE.
  *stackTop = get_array_length (word2obj (*stackTop));
  goto LABEL_ENGINELOOP;


// Notes:
// * OP_ANEWARRAY is changed to OP_NEWARRAY of data type 0, plus a NOOP.

/*end*/







