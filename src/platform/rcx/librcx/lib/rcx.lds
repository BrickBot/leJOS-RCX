/*
 *  rcx.lds
 *
 *  GNU ld script for RCX replacement firmware
 *
 *  Thanks to Markus Noga for observing that an ld script could could be
 *  used to customize the link.  The best documentation for writing GNU ld
 *  scripts is contained in the GNU info pages for ld.
 *
 *  This ld script does not support C++ objects.  My apologies.
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

OUTPUT(firmware.srec)
OUTPUT_FORMAT(srec)
OUTPUT_ARCH(h8300)
ENTRY(__start)
MEMORY
{
    mem : o = 0x8000, l = 0x6f00
}
SECTIONS
{
    .text : {
	*(.init)
        *(.text)
        *(.rodata)
    } > mem
    .data : {
        *(.data)
    } > mem
    .bss : {
        __bss_start = . ;
        *(.bss)
        *(COMMON)
        __end = . ;
    } > mem
}
