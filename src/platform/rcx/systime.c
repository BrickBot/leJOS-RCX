#include "systime.h"

/**
 * sys_time should be initialized every
 * time a new program is started.
 */
volatile unsigned long sys_time;

/**
 * systime_init() should be called after
 * every call to init_timer().
 */
void systime_init(void) {
    T_IER &= ~TIER_ENABLE_OCA; 
    ocia_vector = &systime_handler;
    T_IER |=  TIER_ENABLE_OCA;
}


/***
 * the systime_handler doesn't do too much. After all, its gotta
 * run every millisecond. What we do can perhaps be best described
 * as "borrowing" the OCIA interrupt. Because, the first thing we
 * do is call the default ROM handler. This causes the rom to do
 * a bunch of stuff for us (IR communication, sensor, motor, sound, to
 * name a few.) After its done, we increment our time counter, and 
 * then return. 
 */
__asm__("
.text
.align 1
.global _systime_handler
_systime_handler:
                ; r6 saved by ROM

                ; call the ROM OCIA handler
                jsr     _rom_ocia_handler 

                ; increment system timer
                mov.w @_sys_time+2,r6          ; LSW -> r6
                add.b #0x1,r6l                 ; 16 bit: add 1
                addx  #0x0,r6h
                mov.w r6,@_sys_time+2
                bcc sys_nohigh                 ; speedup for 65535 cases
                  mov.w @_sys_time,r6          ; MSW -> r6
                  add.b #0x1,r6l
                  addx  #0x0,r6h
                  mov.w r6,@_sys_time
              sys_nohigh:
                ;bclr    #3,@0x91:8        ; reset compare A IRQ flag
                rts
        ");

