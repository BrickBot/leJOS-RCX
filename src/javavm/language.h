
#include "types.h"
#include "classes.h"

#ifndef _LANGUAGE_H
#define _LANGUAGE_H

#define STATIC_INITIALIZER 0

// Class flags:

#define C_INITIALIZED 0x01
#define C_ARRAY       0x02
#define C_HASCLINIT   0x04
#define C_INTERFACE   0x08

extern byte get_class_index (Object *obj);
extern void dispatch_special (byte classIdx, byte methodIndex, byte *rAddr);
extern void dispatch_virtual (Object *obj, TWOBYTES signature, byte *rAddr);
extern short find_method (byte classIndex, TWOBYTES signature);
extern STACKWORD instance_of (Object *obj, byte classIndex);
extern void do_return (byte numWords);

typedef struct S_MasterRecord
{
  TWOBYTES magicNumber;
  TWOBYTES constantTableOffset;
  TWOBYTES staticStateOffset;
} MasterRecord;

typedef struct S_ClassRecord
{
  /**
   * Space occupied by instance in 2-byte words.
   */
  TWOBYTES classSize;
  /**
   * Offset of method table (in bytes) starting from
   * the beginning of the entire binary.
   */
  TWOBYTES methodTableOffset;
  /**
   * Offset to table of bytes containing types of fields.
   * Useful for initializing objects.
   */
  TWOBYTES instanceTableOffset;
  byte numInstanceFields;
  byte numMethods;
  byte parentClass;
  /**
   * Class index of array element.
   */
  byte arrayElementType;
  byte cflags;
} ClassRecord;

// Method flags:

#define M_NATIVE       0x01
#define M_SYNCHRONIZED 0x02
#define M_STATIC       0x04

typedef struct S_MethodRecord
{
  // Unique id for the signature of the method
  TWOBYTES signatureId;
  // Offset to table of exception information
  TWOBYTES exceptionTable;
  TWOBYTES codeOffset;
  // Number of locals, in 32-bit words.
  byte numLocals;
  // Maximum size of local operand stack, in 32-bit words.
  byte maxOperands;
  // Number of parameters, including receiver.
  byte numParameters;
  // Number of exception handlers
  byte numExceptionHandlers;
  byte mflags;
} MethodRecord;

typedef struct S_ExceptionRecord
{
  TWOBYTES start;
  TWOBYTES end;
  TWOBYTES handler; 
  // The index of a Throwable class.
  byte classIndex; 
} ExceptionRecord;

typedef struct S_ConstantRecord
{
  /**
   * Offset to bytes of constant.
   */
  TWOBYTES offset;
  /**
   * Size of constant. Length of Utf8 entry for strings.
   */
  byte constantSize;
} ConstantRecord;

extern MasterRecord *installedBinary;

#define install_binary(PTR_)        (installedBinary=(PTR_))
#define get_master_record()         (installedBinary)
#define get_binary_base()           ((byte *) installedBinary)
#define get_class_base()            ((ClassRecord *) (get_binary_base() + sizeof(MasterRecord)))

#define get_class_record(CLASSIDX_) (get_class_base() + (CLASSIDX_))
#define get_method_table(CREC_)     ((MethodRecord *) (get_binary_base() + (CREC_)->methodTableOffset))

#define get_field_table(CREC_)      ((byte *) (get_binary_base() + (CREC_)->instanceTableOffset))

#define get_field_type(CR_,I_)      (*(get_field_table(CR_) + (I_)))

#define get_method_record(CR_,I_)   (get_method_table(CR_) + (I_)) 

#define get_constant_base()         ((ConstantRecord *) (get_binary_base() + installedBinary->constantTableOffset))

#define get_constant_record(IDX_)   (get_constant_base() + (IDX_))

#define class_size(CLASSIDX_)       (get_class_record(CLASSIDX_)->classSize)

#define is_initialized(CLASSREC_)   ((CLASSREC_)->cflags & C_INITIALIZED)
#define is_array_class(CLASSREC_)   ((CLASSREC_)->cflags & C_ARRAY)
#define has_clinit(CLASSREC_)       ((CLASSREC_)->cflags & C_HASCLINIT)
#define is_interface(CLASSREC_)     ((CLASSREC_)->cflags & C_INTERFACE)

#define set_initialized(CLASSREC_)  ((CLASSREC_)->cflags |= C_INITIALIZED)

#endif







