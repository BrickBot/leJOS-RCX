
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

#define T_REFERENCE 0
#define T_STACKFRAME 1
#define T_BOOLEAN 4
#define T_CHAR 5
#define T_FLOAT 6
#define T_DOUBLE 7
#define T_BYTE 8
#define T_SHORT 9
#define T_INT 10
#define T_LONG 11

#define null ((void *) 0)
#define true (1)
#define false (0)

#define jfloat2word(FLOAT_) (*((STACKWORD *) &(FLOAT_)))
#define byte2jint(BYTE_)    ((JINT) (signed char) (BYTE_))
#define word2jint(WORD_)    ((JINT) (WORD_))
#define word2jfloat(WORD_)  (*((JFLOAT *) &(WORD_)))
#define word2obj(WORD_)     ((Object *) (WORD_))

#endif _TYPES_H


