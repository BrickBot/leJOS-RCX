#include "platform_config.h"

#include "sensors.h"

#define SENSOR_MODE_MASK 0xe0

extern byte angle_state[3];     /* defined in rcx.lds */
static byte old_angle_state[3]; /* last valid state */
static signed char delta[4][4] = {{0,1,-1,0},{-1,0,0,1},{1,0,0,-1},{0,-1,1,0}};

sensor_t sensors[3];

void poll_sensors( void)
{
  byte i;
  for( i=0; i<3; i++){

#ifdef ANGLE_DOUBLE_CHECK
    /* we accept a state as valid only if it occurs at least twice in a row */
    if( ( sensors[i].mode & SENSOR_MODE_MASK ) == SENSOR_MODE_ANGLE){
      byte state = angle_state[i];
      angle_state[i] = -1;                 /* prevent value change */

      read_sensor( 0x1000+i, &sensors[i]); /* puts state in angle_state[i] */

      if( angle_state[i] == state          /* state is valid */
          && state != old_angle_state[i]){ /* and has changed */
        sensors[i].value += (int)delta[old_angle_state[i]][state];
        old_angle_state[i] = state;
      }
      return;
    }
#endif
    read_sensor( 0x1000+i, &sensors[i]);
  }
}
