#include "interpreter.h"

#include "systime.h"

/**
 * sys_time should be initialized every
 * time a new program is started.
 */
volatile unsigned long sys_time;

/***
 * the systime_handler doesn't do too much. After all, its gotta
 * run every millisecond. What we do can perhaps be best described
 * as "borrowing" the OCIA interrupt. Because, the first thing we
 * do is call the default ROM handler. This causes the rom to do
 * a bunch of stuff for us (IR communication, sensor, motor, sound, to
 * name a few.) After its done, we increment our time counter, and 
 * then return. 
 */
__asm__(
".text\n"
".align 1\n"
".global _systime_handler\n"
"_systime_handler:\n"
"                ; r6 saved by ROM\n"
"\n"
"                ; call the ROM OCIA handler\n"
"                jsr     _rom_ocia_handler \n"
"\n"
"                ; increment system timer\n"
"                mov.w @_sys_time+2,r6          ; LSW -> r6\n"
"                add.b #0x1,r6l                 ; 16 bit: add 1\n"
"                addx  #0x0,r6h\n"
"                mov.w r6,@_sys_time+2\n"
"                bcc sys_nohigh                 ; speedup for 65535 cases\n"
"                  mov.w @_sys_time,r6          ; MSW -> r6\n"
"                  add.b #0x1,r6l\n"
"                  addx  #0x0,r6h\n"
"                  mov.w r6,@_sys_time\n"
"              sys_nohigh:\n"
"\n"
"                ; set tick request\n"
"                mov.b #0x1,r6l\n"
"                mov.b r6l,@_gMakeRequest\n"
"\n"
"                ;bclr    #3,@0x91:8        ; reset compare A IRQ flag\n"
"                rts\n"
);

