/*
 *  mulhi3.c
 *
 *  Wrapper for ROM mulhi3 routine, a 16-bit multiply: r0 *= r1
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

    .section .text

    .global ___mulhi3

___mulhi3:

    mov.b   r1h,r2l
    mov.b   r0h,r1h

    mulxu.b r0l,r2
    mulxu.b r1l,r0
    mulxu.b r1h,r1

    add.b   r1l,r0h
    add.b   r2l,r0h

    rts
