
#ifndef _CONFIGURE_H
#define _CONFIGURE_H

/**
 * Thread switching is not
 * implemented based on time, but on number
 * of opcodes executed.
 */
#define OPCODES_PER_TIME_SLICE           128

/**
 * Maximum level of recursion.
 */
#define MAX_STACK_FRAMES                 8
 
/**
 * Maximum number of words in a thread's stack
 * (for both locals and operands).
 */
#define STACK_SIZE                       32

/**
 * Should always be 1.
 */
#define STACK_CHECKING                   1

/**
 * Should always be 1.
 */
#define ARRAY_CHECKING                   1

/**
 * Iff not 0, threads in the DEAD state are
 * removed from the circular list. Recommended.
 */
#define REMOVE_DEAD_THREADS              1

/**
 * Size of native stack in 2-byte words.
 */
#define NATIVE_STACK_SIZE                16


/**
 * Whether floating-point arithmetic is
 * supported for floats. (doubles are treated
 * as floats always).
 */
#define FP_ARITHMETIC                    0

/**
 * Slightly safer code?
 */
#define SAFE                             1

#endif







