/*
 *  rcx_comm_darwin.c
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

/* This must come first to override the default #defines */
#include "rcx_comm_darwin.h"
#include "rcx_comm.h"

#include "osx_usb.h"


/* Defines */

#define BUFFERSIZE  4096

/* Globals */

extern int __comm_debug;


void __rcx_perror(char * str) 
{
 	if (__comm_debug) perror(str);
}

/* Timeout read routine */

int __rcx_read(void *port, void *buf, int maxlen, int timeout)
{
	int len = 0;

	if (rcx_is_usb((rcx_dev_t)port)) {
		len = osx_usb_nbread(port->fd, buf, maxlen, timeout);
		return len;
	}

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
	// Perhaps we should unstall the USB channel here ?
}

/* discard all characters in the input queue of tty */
void __rcx_flush(rcx_dev_t *port)
{
}

int __rcx_write(rcx_dev_t *port, const void *bufv, size_t len) 
{
	if (rcx_is_usb(port)) {
		return osx_usb_write(port->fd, bufv, len);
	}

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

	port = (rcx_dev_t *)malloc(sizeof(rcx_dev_t));
	if (!port) {
		perror("malloc");
		exit(1);
	}
	port->tty = (char *)malloc(strlen(tty)+1);
	if (!port->tty) {
		perror("malloc");
		exit(1);
	}
	strcpy(port->tty, tty);

	port->fast = is_fast;

	/* Assume USB if no tty specified */
	if (strstr(tty, "/") == NULL) {
		if (port->fast) {
			fprintf(stderr, "FAST mode not allowed with USB OS X\n");
			exit(1);
		} 
		if (__comm_debug) printf("port->usb = %d\n", port->usb);
		port->fd = (FILEDESCR) osx_usb_rcx_init(0);
		if (port->fd == NULL) {
			exit(1);
		}
	} else {

		if ((port->fd = open(tty, O_RDWR)) < 0) { 
			perror(tty);
			exit(1);
		}
		
		if (!isatty(port->fd)) {
			close(port->fd);
			fprintf(stderr, "%s: not a tty\n", tty);
			exit(1);
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
			exit(1);
		}
	}
	return port;
}

void __rcx_close(rcx_dev_t *port)
{
	if (rcx_is_usb(port)) {
		osx_usb_rcx_close(port->fd);
	} else {
		close(port->fd);
	}
}
