/*
 *  rcx_comm_linux.c
 *
 *  Platform specific RCX communication routines.
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
 *  The Original Code is Firmdl code, released October 3, 1998.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1998, 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  Contributor(s): Kekoa Proudfoot <kekoa@graphics.stanford.edu>
 *
 *  02/01/2002 Lawrie Griffiths Changes for rcxcomm
 *  09/23/2002 david <david@csse.uwa.edu.au> modified to support linux usb tower
 *    - changed rcx_recv to expect explicit number of bytes rather than 4096
 *  27/01/2003 david <david@csse.uwa.edu.au> changed to factor out platform specific
 *  code
 *
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <termios.h>
#include <sys/time.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>

#include "rcx_comm.h"
#include "rcx_comm_linux.h"

#include <asm/ioctl.h>
#include "legousbtower.h"


/* Defines */

#define BUFFERSIZE  4096

/* Globals */

extern int __comm_debug;

extern int usb_flag;

void __rcx_perror(char * str) 
{
 	if (__comm_debug) perror(str);
}

/* Timeout read routine */

int __rcx_read (FILEDESCR fd, void *buf, int maxlen, int timeout)
{
	long len = 0;
    
	char *bufp = (char *)buf;

	int count;
	fd_set fds;
	struct timeval tv;
	int retry = 10;

	while (len < maxlen) {
		FD_ZERO(&fds);
		FD_SET(fd, &fds);

		tv.tv_sec = timeout / 1000;
		tv.tv_usec = (timeout % 1000) * 1000;

		if (select(fd+1, &fds, NULL, NULL, &tv) < 0) {
			perror("select");
			return RCX_READ_FAIL;
		}
		if (!FD_ISSET(fd, &fds)) {
			if (len > 0 || retry == 0) {
				break;
			} else {
				retry--;
			}
		}	
		if ((count = read(fd, &bufp[len], maxlen - len)) < 0) {
			perror("read");
			return RCX_READ_FAIL;
		}
		len += count;
	}
	return len;
}

/* discard all characters in the input queue of tty */
void __rcx_flush(FILEDESCR fd)
{
	char echo[BUFFERSIZE];
	__rcx_read(fd, echo, BUFFERSIZE, 200);
}

int __rcx_write(FILEDESCR fd, const void *buf, size_t len) 
{

	return write(fd, buf, len);

}

/* RCX routines */

FILEDESCR __rcx_init(char *tty, int is_fast)
{
	FILEDESCR fd;

	struct termios ios;

	if (__comm_debug) printf("mode = %s\n", is_fast ? "fast" : "slow");
	if (__comm_debug) printf("tty= %s\n", tty);


	if ((fd = open(tty, O_RDWR)) < 0) { 
		perror(tty);
		exit(1);
	}

	/* Assume USB for all non-tty devices */
	usb_flag = !isatty(fd);
	if (__comm_debug) printf("usb_flag = %d\n", usb_flag);

	if (usb_flag) {
		if (is_fast) {
			fprintf(stderr, "FAST mode not allowed with USB LINUX\n");
			return RCX_OPEN_FAIL;
		} 
	} else {

		if (!isatty(fd)) {
			close(fd);
			fprintf(stderr, "%s: not a tty\n", tty);
			return BADFILE;
		}

		memset(&ios, 0, sizeof(ios));
	    
		if (is_fast) {
			ios.c_cflag = CREAD | CLOCAL | CS8;
			cfsetispeed(&ios, B4800);
			cfsetospeed(&ios, B4800);
		}
		else {
			ios.c_cflag = CREAD | CLOCAL | CS8 | PARENB | PARODD;
			cfsetispeed(&ios, B2400);
			cfsetospeed(&ios, B2400);
		}
	    
		if (tcsetattr(fd, TCSANOW, &ios) == -1) {
			perror("tcsetattr");
			return BADFILE;
		}
	}

	return fd;
}

void __rcx_close(FILEDESCR fd)
{
	close(fd);
}
