/*
 *  modhi3.c
 *
 *  Wrapper for ROM modhi3 routine, a 16-bit signed modulo: r0 %= r1
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

    .global ___modhi3

___modhi3:

    push    r5
    push    r6

    mov.w   r0,r6
    mov.w   r1,r5

    jsr     @@80

    mov.w   r6,r0

    pop     r6
    pop     r5

    rts
");
