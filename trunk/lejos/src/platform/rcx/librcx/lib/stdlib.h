/*
 *  stdlib.h
 *
 *  A hack to glue together miscellaneous standard functions and defines.
 *
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS"
 *  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 *  License for the specific language governing rights and limitations
 *  under the License.
 *
 *  The Original Code is Librcx code, released February 9, 1999.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  Contributor(s): Kekoa Proudfoot <kekoa@graphics.stanford.edu>
 */

#ifndef STDLIB_H_DEFINED
#define STDLIB_H_DEFINED

/* Hack! These really belong in separate files with different names... */

typedef unsigned int size_t;

extern void __bcopy (void *start, void *end, void *dst) __asm__("0x042a");

#if 0
extern void *memcpy (void *s1, const void *s2, size_t n);
#else
static inline void *memcpy (void *s1, const void *s2, size_t n) {
    char *start = (char *)s2;
    char *end = start + n;
    __bcopy(start, end, s1);
    return s1;
}
#endif

extern void *memset (void *s, int c, size_t n);

/* A non standard function, but convenient */
extern void __bzero (void *start, void *end) __asm__("0x0436");

/* Also a non standard function, also convenient */
static inline void bzero (void *start, size_t len) {
    __bzero(start, (char *)start + len);
}

/* Hack! These should be defined elsewhere */
#define __reg_bset(reg,bit) __asm__ ("bset #" #bit ",@" #reg ":8")
#define __reg_bclr(reg,bit) __asm__ ("bclr #" #bit ",@" #reg ":8")

#define NULL 0

#if 0
typedef unsigned int jmp_buf[8];

extern int setjmp (jmp_buf env);
void longjmp (jmp_buf env, int val);
#endif

extern void *malloc (size_t size);
extern void free (void *ptr);

#endif

