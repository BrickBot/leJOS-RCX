
#ifndef _TYPES_H
#define _TYPES_H

typedef unsigned char byte;
typedef byte boolean;
typedef unsigned short TWOBYTES;
typedef unsigned long FOURBYTES;
typedef FOURBYTES REFERENCE; // 4 bytes
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

#define jfloat2word(FLOAT_) (((AuxConvUnion1) (FLOAT_)).sword)
#define word2jfloat(WORD_)  (((AuxConvUnion1) (WORD_)).fnum)
#define byte2jint(BYTE_)    ((JINT) (signed char) (BYTE_))
#define word2jint(WORD_)    ((JINT) (WORD_))
#define word2obj(WORD_)     ((Object *) word2ptr(WORD_))
#define obj2word(OBJ_)      ptr2word(OBJ_)

#ifdef EMULATE

#define ptr2word(PTR_)  ((STACKWORD) (PTR_))
#define word2ptr(WRD_)  ((void *) (WRD_))

#else

#define ptr2word(PTR_)  ((STACKWORD) (TWOBYTES) (PTR_))
#define word2ptr(WRD_)  ((void *) (TWOBYTES) (WRD_))

#endif

#endif _TYPES_H


