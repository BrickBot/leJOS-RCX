/*
 *  init.c
 *
 *  Startup routines.  Saves all registers gcc assumes saved, clears
 *  uninitialized variables, then calls main.  If main ever returns, the
 *  firmware is deleted.
 *
 *  Does not call constructors and destructors, since I assume C only.
 *
 *  This file is somewhat like a crt0.o, which should live in:
 *
 *     .../lib/gcc-lib/h8300-hitachi-hms/2.8.1/
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

#include <stdlib.h>

/* Place start code in a special .init section so linker places it first */
/* Place the "Do you byte?" string here too, to make sure it's before cc00 */
/* Placing this start code first is not needed with the latest firmdl.c */
/* But it's nice to have it linked in first, which is why it's still here */

__asm__ (
    ".section .init\n\t"
    ".global __start\n"
    "__start:\n\t"
    "push r0\n\t"
    "push r1\n\t"
    "push r2\n\t"
    "push r3\n\t"
    "jsr __init\n\t"
    "pop r3\n\t"
    "pop r2\n\t"
    "pop r1\n\t"
    "pop r0\n\t"
    "rts\n\t"
    ".string \"Do you byte, when I knock?\""
);

extern int main (void);

extern char _bss_start, _end;

void _init (void) {
    char *p;

    /* Clear the .bss data */
    __bzero(&_bss_start, &_end);

#if 0
    /* Clear the shadow registers */
    __bzero((void *)0xfd80, (void *)0xfd86);
#endif

    /* Call main */
    main();

    /* Delete the firmware */
    *(char *)0xffcc = 1;
    (*(void (**)(void))0)();
}

