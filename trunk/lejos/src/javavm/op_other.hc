/**
 * This is included inside a switch statement.
 */

case OP_ATHROW:
  // REMOVE: 
  #if 0
  trace (-1, get_class_index(word2obj(*stackTop)), 2);
  #endif
  if (word2obj(*stackTop) == JNULL)
  {
    throw_exception (nullPointerException);
    return;
  }
  throw_exception (word2obj(*stackTop--));
  goto LABEL_ENGINELOOP;
case OP_MONITORENTER:
  enter_monitor (word2obj(*stackTop--));
  goto LABEL_ENGINELOOP;
case OP_MONITOREXIT:
  exit_monitor (word2obj(*stackTop--));
  goto LABEL_ENGINELOOP;

// Notes:
// - Not supported: BREAKPOINT

/*end*/


