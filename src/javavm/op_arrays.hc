/**
 * This is included inside a switch statement.
 */

// TBD: ArrayIndexOutOfBounds!!

case OP_NEWARRAY:
  // Stack size: unchanged
  // Arguments: 1
  *stackTop = obj2word(new_primitive_array (*pc++, *stackTop));
  goto LABEL_ENGINELOOP;
case OP_MULTIANEWARRAY:
  // Stack size: -N + 1
  // Arguments: 3
  gBytePtr = (byte *) new_multi_array (pc[0], pc[1], pc[2], stackTop);
  stackTop -= pc[2];
  *(++stackTop) = ptr2word (gBytePtr);
  pc += 3;
  goto LABEL_ENGINELOOP;
case OP_IALOAD:
case OP_FALOAD:
case OP_AALOAD:
case OP_LALOAD:
case OP_DALOAD:
case OP_BALOAD:
case OP_CALOAD:
case OP_SALOAD:
  // Stack size: -2 + 1
  // Arguments: 0
  gBytePtr = word2ptr(*(stackTop-1));
  gInt = word2jint(stackTop[0]);
  if (gBytePtr == JNULL)
    throw_exception (nullPointerException);
  else if (gInt < 0 || gInt >= get_array_length ((Object *) gBytePtr))
    throw_exception (arrayIndexOutOfBoundsException);
  else
  {
     stackTop--;
     switch (*(pc-1))
     {
       case OP_IALOAD:
       case OP_AALOAD:
       case OP_FALOAD:
         stackTop[0] = word_array(gBytePtr)[gInt];
         break;
       case OP_SALOAD:
       case OP_CALOAD:
         stackTop[0] = jshort_array(gBytePtr)[gInt];
         break;
       case OP_BALOAD:
         stackTop[0] = jbyte_array(gBytePtr)[gInt];
         break;
       case OP_DALOAD:
       case OP_LALOAD:
         gInt *= 2;
         *stackTop = word_array(gBytePtr)[gInt++];
         *(++stackTop) = word_array(gBytePtr)[gInt];
         break;
       #ifdef VERIFY
       default:
         assert (false, INTERPRETER0);
       #endif
     }
  }
  goto LABEL_ENGINELOOP;
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







