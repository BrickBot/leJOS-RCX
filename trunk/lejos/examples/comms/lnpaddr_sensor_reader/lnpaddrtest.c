#include <conio.h>
#include <unistd.h>
#include <lnp.h>
#include <dsensor.h>

#define MYPORT 0
#define DEST 0x20

int got = 0;
unsigned char buf[2];
int in;

void getdata(unsigned char *inbuf, char l, unsigned char src) {
  in = *inbuf;
  got = 1;
}  

wakeup_t WaitForData(wakeup_t data) { 
  return got; 
} 

int main(int argc, char **argv) {
  int val;

  lnp_addressing_set_handler(MYPORT, &getdata);
  ds_active(&SENSOR_2);

  while(1) {
    wait_event(WaitForData, 0);

    switch (in) {
      case 0: val = SENSOR_1; break;
      case 1: val = SENSOR_2; break;
      case 2: val = SENSOR_3; break;
    }

    lcd_int(val);

    buf[0] = val/256;
    buf[1] = val%256;

    lnp_addressing_write(buf, 2, DEST, MYPORT);
    got = 0;
  }
  return 0;
}
