/*
 *  test5.c
 *
 *  A hack to test ir communication.
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

#include "stdlib.h"
#include "rom.h"

/* ROM pseudofunctions */

static inline void
set_data_pointer (void *ptr)
{
    play_sound_or_set_data_pointer(0x1771, (short)ptr, 0);
}
static inline char
check_valid (void)
{
    char valid;
    check_for_data(&valid, NULL);
    return valid;
}
static inline int
receive_message (void *ptr, int len)
{
    char bytes = 0;
    receive_data(ptr, len, &bytes);
    /* Bytes includes checksum, since we don't want that, return bytes-1 */
    return bytes - 1;
}
static inline void
send_message (void *ptr, int len)
{
    if (len)
	while (send_data(0x1775, 0, ptr, len));
}

int
main (void)
{
    int count = 0;
    char buf[16];
    char temp[64];

    memset(temp,0, sizeof(temp));

    /* Initialize */

    init_timer(&temp[6], &temp[0]);
    init_power();
    init_serial(&temp[4], &temp[6], 1, 1);

    set_lcd_number(LCD_UNSIGNED, 0, LCD_DECIMAL_0);
    refresh_display();

    while (1) {

	/* If a message has arrived, send a response with opcode inverted */

	if (check_valid()) {
	    int len = receive_message(buf, sizeof(buf));
	    set_lcd_number(LCD_UNSIGNED, buf[0], LCD_DECIMAL_0);
	    refresh_display();
	    buf[0] = ~buf[0];
	    send_message(buf, len);
	    set_lcd_number(LCD_UNSIGNED, ++count, LCD_DECIMAL_0);
	    refresh_display();
	}
    }

    return 0;
}
