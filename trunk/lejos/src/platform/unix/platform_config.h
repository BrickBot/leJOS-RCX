/* Generated by ./dump_config. Do not modify. */
#ifndef _PLATFORM_CONFIG_H
#define _PLATFORM_CONFIG_H

#include <stdio.h>
typedef signed char JBYTE;
typedef signed short JSHORT;
typedef signed long JINT;
typedef unsigned short TWOBYTES;
typedef unsigned long FOURBYTES;
#include "systime.h"
#define ptr2word(PTR_) ((STACKWORD) (PTR_))
#define word2ptr(WRD_) ((void *) (WRD_))
#define get_sys_time() get_sys_time_impl()
#define LITTLE_ENDIAN 1
#define FP_ARITHMETIC 1
#define PLATFORM_HANDLES_SWITCH_THREAD 0
#define OPCODES_PER_TIME_SLICE 148
#define VERIFY

#endif _PLATFORM_CONFIG_H
