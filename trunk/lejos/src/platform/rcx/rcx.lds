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
    rom (r)     : o = 0x0000, l = 0x8000
    mem         : o = 0x8000, l = 0x6f00
    romdata1    : o = 0xef00, l = 0x0100     /* exact boundaries unclear */
    extra1      : o = 0xf010, l = 0x0b70
    hwreserved1 : o = 0xfb80, l = 0x0200     /* reserved by hardware */
    romdata2    : o = 0xfd80, l = 0x0010     /* used in init_timer */
    intvectors  : o = 0xfd90, l = 0x0030     /* interrupt vectors */
    stack       : o = 0xfdc0, l = 0x0220     /* grows down below 0xff80 */
    extra2      : o = 0xff80, l = 0x0008
    hwreserved2 : o = 0xff88, l = 0x0008     /* reserved by hardware */
    hwregisters : o = 0xff90, l = 0x0070     /* hardware registers */
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

    .romdata1 : {
        _angle_state = 0x32 ; /* 0xef32 */
    } > romdata1

    .extra : {
        __extraMemory = . ;
    } > extra

    .intvectors : {
       _ocia_vector  = 0x12 ; /* 0xfda2 */
    } > intvectors

    .hwregisters : {
       _T_IER        = 0x00 ; /* 0xff90 */
    } > hwregisters
}
