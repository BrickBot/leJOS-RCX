/*
 *  udivsi3.c
 *
 *  Wrapper for ROM udivsi3 routine, a 32-bit unsigned divide: r0r1 /= r2r3
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

__asm__ ("
    .section .text

    .global ___udivsi3

___udivsi3:

    push    r4
    push    r5
    push    r6

    mov.w   r1,r6
    mov.w   r0,r5
    mov.w   r3,r4
    mov.w   r2,r3

    jsr     @@86

    mov.w   r6,r1
    mov.w   r5,r0

    pop     r6
    pop     r5
    pop     r4

    rts
");
