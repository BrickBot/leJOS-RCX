
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
#define word2obj(WORD_)     ((Object *) (WORD_))
#define obj2word(OBJ_)      ((STACKWORD) (OBJ_))

#endif _TYPES_H


