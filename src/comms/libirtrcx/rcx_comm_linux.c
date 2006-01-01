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
 *  03/03/2005 Matthias Paul Scholz read tty from env variable RCXTTY at need
 *  03/03/2005 Matthias Paul Scholz replaced exit statements in rcx_open with return NULL
 *  27/01/2003 david <david@csse.uwa.edu.au> changed to factor out platform specific
 *  code
 *  09/23/2002 david <david@csse.uwa.edu.au> modified to support linux usb tower
 *    - changed rcx_recv to expect explicit number of bytes rather than 4096
 *  02/01/2002 Lawrie Griffiths Changes for rcxcomm
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
#include "rcx_comm_os.h"
#include "rcx_comm_linux.h"

    
/* Defines */

#define BUFFERSIZE  4096

/* Globals */

static int __comm_debug = 0;


void __rcx_perror(char * str) 
{
 	if (__comm_debug) perror(str);
}

/* Timeout read routine */

int __rcx_read(rcx_dev_t *port, void *buf, int maxlen, int timeout)
{
	long len = 0;
    
	char *bufp = (char *)buf;

	int count;
	fd_set fds;
	struct timeval tv;
	int retry = 10;

	while (len < maxlen) {
		FD_ZERO(&fds);
		FD_SET(port->fd, &fds);

		tv.tv_sec = timeout / 1000;
		tv.tv_usec = (timeout % 1000) * 1000;

		if (select(port->fd+1, &fds, NULL, NULL, &tv) < 0) {
			perror("select");
			return RCX_READ_FAIL;
		}
		if (!FD_ISSET(port->fd, &fds)) {
			if (len > 0 || retry == 0) {
				break;
			} else {
				retry--;
			}
		}	
		count = read(port->fd, &bufp[len], maxlen - len);
		if (count < 0) {
			if (errno != ETIMEDOUT) {
				perror("read");
			}
			return RCX_READ_FAIL;
		}
		len += count;
	}
	return len;
}

/* discard all characters in the input queue of tty */
void __rcx_purge(rcx_dev_t *port)
{
	char echo[BUFFERSIZE];
	__rcx_read(port, echo, BUFFERSIZE, 200);
}

/* discard all characters in the input queue of tty */
void __rcx_flush(rcx_dev_t *port)
{
}

int __rcx_write(rcx_dev_t *port, const void *bufv, size_t len) 
{
	const unsigned char *buf = (const unsigned char *)bufv;
	int l = 0;
	while (l < len) {
		int result = write(port->fd, buf+l, len-l);
		if (result < 0) {
			perror("write");
			exit(1);
		}
		l += result;
	}
	return l;
}

/* RCX routines */

rcx_dev_t *__rcx_open(char *tty, int is_fast)
{
	struct termios ios;
	rcx_dev_t *port = NULL;

	if (__comm_debug) printf("mode = %s\n", is_fast ? "fast" : "slow");
	if (__comm_debug) printf("tty= %s\n", tty);
		
	/* if tty is not set, read it from env variable RCXTTY */
	/* if RCXTTY is not set also, return NULL */
	if(strlen(tty)==0) {
		tty = getenv("RCXTTY");
		if (__comm_debug) printf("tty is now %s\n", tty);
		if(!tty)
			return NULL;
	}
		

	port = malloc(sizeof(rcx_dev_t));
	if (!port) {
		perror("malloc");
		return NULL;
	}
	port->tty = malloc(strlen(tty)+1);
	if (!port->tty) {
		perror("malloc");
		return NULL;
	}
	strcpy(port->tty, tty);

	port->fast = is_fast;
	if ((port->fd = open(tty, O_RDWR)) < 0) { 
		perror(tty);
		return NULL;
	}

	/* Assume USB for all non-tty devices */
	port->usb = !isatty(port->fd);
	if (__comm_debug) printf("port->usb = %d\n", port->usb);

	if (port->usb) {
		if (port->fast) {
			fprintf(stderr, "FAST mode not allowed with USB LINUX\n");
			return NULL;
		} 
	} else {
		if (!isatty(port->fd)) {
			close(port->fd);
			fprintf(stderr, "%s: not a tty\n", tty);
			return NULL;
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
	    
		if (tcsetattr(port->fd, TCSANOW, &ios) == -1) {
			perror("tcsetattr");
			return NULL;
		}
	}
	return port;
}

void __rcx_close(rcx_dev_t *port)
{
	close(port->fd);
}
