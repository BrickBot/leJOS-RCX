/*
 *  test.c
 *
 *  A hacked framework for testing ROM functions.
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
 *  The Original Code is Librcx sample program code, released February 9,
 *  1999.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  Contributor(s): Kekoa Proudfoot <kekoa@graphics.stanford.edu>
 */

#include "rom.h"

static char data[64]; /* Hack! */

void debug_value(short value) {
    set_lcd_number(0x3001, value / 10, 0x3002);
    set_lcd_number(0x3017, value % 10, 0x0000);
    refresh_display();
}

void wait_press (void)
{
    short debounce;
    short status;
    for (debounce = 0; debounce < 10; debounce++) {
	get_power_status(0x4000, &status);
	if (status)
	    debounce = 0;
    }   
}

void wait_release (void)
{
    short debounce;
    short status;
    for (debounce = 0; debounce < 10; debounce++) {
	get_power_status(0x4000, &status);
	if (!status)
	    debounce = 0;
    }   
}

/* A hack to demonstrate fine motor control */

short count;
short percent;
short update;

#define VARY_MOTOR_SPEED

void motor_control(void) {
    /* This is somewhat sketchy, we don't save regs */
    *(char *)0xff91 &= ~8; /* Clear OCIA flag */
    count += percent;
    if (count >= 100) {
	*(char *)0xf000 = 0x55; /* All motors forward */
	count -= 100;
    }
    else {
	*(char *)0xf000 = 0x00; /* Stop motors */
    }
#ifdef VARY_MOTOR_SPEED
    update++;
    if (update > 500) {
	update = 0;
	percent++;
	if (percent > 100)
	    percent = 0;
    }
#endif
}

int main (void) {
    char playing;
    short count = -10;
    int count2 = 0;
    int sound = 0;
    int i, j;
    unsigned char op = 0;
    char code;

    while (1) {
	init_timer(&data[6], &data[0]);
	init_power();
	play_system_sound(0x4003, 1);

#if 1
	init_buttons();
	clear_display();
#endif


#if 0
	*((volatile void **)0xfda2) = motor_control;
	while (1);
#endif

#if 0
	while (1) {
	    for (j = 0; j < 100; j++) {
		percent=j;
		set_lcd_number(0x3001, j, 0x3002);
		refresh_display();
		for (i = 0; i < 1000; i++);
	    }
	}
#endif

#if 0
	while (1) {
	    int i;

	    *((short *)0xc000) = 0x0;
#if 0
	    count2 += *((short *)0xfb7f);
#endif
	    for (i = 0; i < 1000; i++) {
		count++;
	    }
	}
#endif

#if 0
	while (1) {
	    set_lcd_number(0x3017, count, 0x3002);
	    refresh_display();
	    count ++;
#if 1
	    wait_release();
	    wait_press();
#endif
	}
#endif

#if 1
	count = 1;
	while (1) {
	    set_lcd_number(0x301f, count, 0x3002);
	    refresh_display();
	    count ++;
	    wait_release();
	    wait_press();
	}
#endif

#if 0
	set_lcd_number(0x301f, 0xffff, 0x3002);
	refresh_display();
	wait_release();
	wait_press();
	break;
#endif

#if 0
	play_system_sound(0x4003, 0);

	do {
	    get_sound_playing_flag(0x700c, &playing);
	} while (playing);
	
	clear_display();
	refresh_display();
	shutdown_power();
#endif

    }

    return 0;
}
