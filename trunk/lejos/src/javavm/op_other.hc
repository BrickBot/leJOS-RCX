/**
 * This is included inside a switch statement.
 */

case OP_ATHROW:
  throw_exception (word2obj(*stackTop--));
  if (gMustExit)
    return;
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


