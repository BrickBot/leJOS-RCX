/**
 * Jose's changes:
 * -- _end is ALIGN(2) now.
 *
 * 2001-08-09 juergen: added extra0 with angle_state for rotation sensor
 */

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
    rom (r) : o = 0x0000, l = 0x8000
    mem     : o = 0x8000, l = 0x6f00
    extra0  : o = 0xef10, l = 0x0040
    extra1  : o = 0xef50, l = 0x00b0
    extra2  : o = 0xf010, l = 0x0d70
    romdata : o = 0xfd80, l = 0x0040     /* for interupt vectors */
    extra3  : o = 0xfe00, l = 0x0100
    eight   : o = 0xff00, l = 0x0100
}
SECTIONS
{
    .rom : {
        _rom_ocia_handler = 0x36ba ;
    } > rom
    .text : {
        __text_begin = . ;
	*(.init)
        *(.text)
        *(.rodata)
        *(.vectors)
        __text_end = . ;
    } > mem
    .data : {
        __data_begin = . ;
        *(.data)
        __data_end = . ;
    } > mem
    .bss : {
        __bss_start = . ;
        *(.bss)
        *(COMMON)
        __end = ALIGN(2) ;
    } > mem

    .extra0 : {
        _angle_state = 0x22 ; 
    } > extra0

    .extra : {
        __extraMemory = . ; 
    } > extra2

    .romdata : {
       _ocia_vector  = 0x22 ;
    } > romdata

    .eight : {
       _T_IER        = 0x90 ;
    } > eight
}
