/*
 *  first.c
 *
 *  A translation of first.s to C
 *
 *  Two major differences: added debouncing, added another shutdown call
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
    char playing;

    while (1) {
	init_timer(&data[6], &data[0]);
	init_power();

	set_lcd_segment(0x3020);
	refresh_display();

	play_system_sound(0x4003, 1);

	wait_release();
	wait_press();

	play_system_sound(0x4003, 0);

	do {
	    get_sound_playing_flag(0x700c, &playing);
	} while (playing);

	clear_display();
	refresh_display();

	shutdown_timer();
	shutdown_power();
    }
    return 0;
}
