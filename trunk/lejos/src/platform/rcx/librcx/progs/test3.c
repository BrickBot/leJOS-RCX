/*
 *  test3.c
 *
 *  Program to test modsi3.
 *
 *  If you run the same test on a MIPS processor, you get:
 *     0 -4 -3 -2 -1 0 1 2 3 4 0 0 -4 -3 -2 -1 0 1 2 3 4
 *  If you run this on the RCX, you get the same.  Hooray.
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

int main (void) {
    long i;

    init_timer(&data[6], &data[0]);
    init_power();
    play_system_sound(0x4003, 1);

    clear_display();

    for (i = -5; i <= 5; i++) {
	set_lcd_number(LCD_SIGNED, i % 5, LCD_DECIMAL_0);
	refresh_display();
	wait_release();
	wait_press();
    }

    for (i = -5; i <= 5; i++) {
	set_lcd_number(LCD_SIGNED, i % -5, LCD_DECIMAL_0);
	refresh_display();
	wait_release();
	wait_press();
    }

    shutdown_timer();

    return 0;
}
