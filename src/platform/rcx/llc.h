#ifndef _LLC_H
#define _LLC_H

extern void llc_init(void);
extern int llc_read(void);
extern void llc_writebytes(unsigned char *, int);
extern int llc_is_sending(void);
extern int llc_send_error(void);

#endif

