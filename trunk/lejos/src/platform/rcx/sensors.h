#ifndef _SENSORS_H
#define _SENSORS_H

#include <rom.h> // Requires librcx

extern sensor_t sensors[3];
extern void poll_sensors( void);

#endif // _SENSORS_H
