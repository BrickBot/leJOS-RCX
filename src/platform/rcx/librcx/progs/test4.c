/*
 *  test4.c
 *
 *  Example to demonstrate how to use the sensor functions.
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

static char dispatch[6];
static async_t async;

void debug_value(short value) {
    set_lcd_number(0x3001, value, 0x3002);
    refresh_display();
}

int main (void) {
    sensor_t sensor;

    init_timer(&async, &dispatch[0]);
    init_power();
    init_sensors();

    set_sensor_passive(SENSOR_0);

    sensor.type = SENSOR_TYPE_TOUCH;
    sensor.mode = SENSOR_MODE_EDGE;
    sensor.raw = 0;
    sensor.value = 0;
    sensor.boolean = 0;

    while (1) {
	read_sensor(SENSOR_0, &sensor);
	debug_value(sensor.value);
    }

    return 0;
}
