
#ifndef _TYPES_H
#define _TYPES_H

typedef unsigned char byte;
typedef byte boolean;
typedef unsigned short TWOBYTES;
typedef unsigned long FOURBYTES;
typedef FOURBYTES REFERENCE;
typedef FOURBYTES STACKWORD;

typedef char JBYTE;
typedef short JCHAR;
typedef short JSHORT;
typedef boolean JBOOLEAN;
typedef long JINT;
typedef float JFLOAT;

typedef union
{
  JFLOAT fnum;
  STACKWORD sword;
} AuxConvUnion1;

typedef struct
{
  STACKWORD hi;
  STACKWORD lo;
} JLONG;

#include "platform_config.h"

#ifndef LITTLE_ENDIAN
#error LITTLE_ENDIAN not defined in platform_config.h
#endif

#define jfloat2word(FLOAT_) (((AuxConvUnion1) (FLOAT_)).sword)
#define word2jfloat(WORD_)  (((AuxConvUnion1) (WORD_)).fnum)
#define byte2jint(BYTE_)    ((JINT) (signed char) (BYTE_))
#define word2jint(WORD_)    ((JINT) (WORD_))
#define word2obj(WORD_)     ((Object *) word2ptr(WORD_))
#define obj2word(OBJ_)      ptr2word(OBJ_)
#define obj2ref(OBJ_)       ptr2ref(OBJ_)
#define obj2ptr(OBJ_)       ((void *) (OBJ_))
#define ptr2ref(PTR_)       ((REFERENCE) ptr2word(PTR_))
#define ref2ptr(REF_)       word2ptr((STACKWORD) (REF_))
#define ref2obj(REF_)       ((Object *) ref2ptr(REF_))

static inline JINT jlong_compare (JLONG a1, JLONG a2)
{
  if (a1.hi == a2.hi)
    return a1.lo - a2.lo;
  return a1.hi - a2.hi;
}

#endif _TYPES_H


