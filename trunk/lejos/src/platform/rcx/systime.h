#ifndef _SYS_TIME_
#define _SYS_TIME_

/*********
 * Systime functionality by Ryan VanderBijl
 * Borrowed code from legOS. Orignally by
 *   Markus L. Noga. Perhaps some from David Van Wagner.
 */


// variables:
#define TIER_ENABLE_OCA         0x08

extern volatile unsigned long sys_time;

extern          unsigned char T_IER;
extern                   void *    ocia_vector ;
extern                   void *rom_ocia_handler;


// functions
void systime_init   (void);
void systime_handler(void);

#endif /* _SYS_TIME_ */
