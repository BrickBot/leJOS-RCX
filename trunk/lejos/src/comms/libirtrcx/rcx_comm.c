/*
 *  rcx_comm.c
 *
 *  RCX communication routines.
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
 *
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>

#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>

#if defined(_WIN32)
#include <windows.h>
#include "rcx_comm_win.h"
#endif

#include "rcx_comm.h"

/* Defines */

#define BUFFERSIZE  4096


/* Machine-dependent defines */

#if defined(LINUX) || defined(linux)
#define TOWER_NAME "/dev/usb/legousbtower0"
#define DEFAULTTTY   "usb"       /* Linux - USB */
/*
 * Programming note (aB.)
 * Shouldn't we use strcmp() which is a POSIX standard API (and works on Linux, Mac and any other Unix),
 * and special-case Windows ?
 */
#define stricmp(x, y) strcmp(x, y)
#define strnicmp(x, y, n) strncmp(x, y, n)
#elif defined(_WIN32) || defined(__CYGWIN32__)
#define DEFAULTTTY   "usb"       /* Cygwin - USB */
#define TOWER_NAME "\\\\.\\LEGOTOWER1"
#elif defined (sun)
#define DEFAULTTTY   "/dev/ttya"  /* Solaris - first serial port - untested */
#elif defined (__APPLE__)
#define DEFAULTTTY   "usb"	  /* Default to USB on MAC */
#define stricmp(x, y) strcmp(x, y)
#define strnicmp(x, y, n) strncmp(x, y, n)
#define TOWER_NAME ""
#else
#define DEFAULTTTY   "/dev/ttyd2" /* IRIX - second serial port */
#endif

/* Globals */

int __comm_debug = 0;
int usb_flag = 1;
int fast_flag = 0;
int use_comp = 0;

/* Timer routines */

typedef struct timeval timeval_t;

#define tvupdate(tv)  gettimeofday(tv,NULL)
#define tvsec(tv)     ((tv)->tv_sec)
#define tvmsec(tv)    ((tv)->tv_usec * 1e-3)

static float timer_reset(timeval_t *timer)
{
	tvupdate(timer);
	return 0;
}

static float timer_read(timeval_t *timer)
{
	timeval_t now;
	tvupdate(&now);
	return tvsec(&now) - tvsec(timer) + (tvmsec(&now) - tvmsec(timer)) * 1e-3;
}

// getter functions

int rcx_is_debug() 
{
	return __comm_debug;
}

void rcx_set_debug(int debug)
{
	__comm_debug = debug;
}

void rcx_set_usb(int usb)
{
	usb_flag = usb;
}

int rcx_is_usb()
{
	return usb_flag;
}

void rcx_set_fast(int fast)
{
	fast_flag = fast;
}

int rcx_is_fast()
{
	return fast_flag;
}

// RCX functions

void rcx_perror(char *str) 
{
	__rcx_perror(str);
}

void rcx_flush(FILEDESCR fd)
{
	__rcx_flush(fd);
}

FILEDESCR rcx_init(char *tty, int is_fast)
{
	char *comm_debug = NULL;

	// if no port supplied, read RCXTTY environment variable

	if ((!tty) || *tty == 0) tty = getenv("RCXTTY");

	// If still no port, default to USB.
	
	if ( (!tty) || *tty == 0) tty = DEFAULTTTY;
	
	rcx_set_usb(0);
	
	if ((stricmp( tty , "usb" ) == 0) ) {
		rcx_set_usb(1);
		tty = TOWER_NAME;
	}  else if ((strnicmp(tty,"usb",3) == 0) && strlen(tty) == 4) {
          	static char buff[20];
		rcx_set_usb(1);
    		strcpy(buff, TOWER_NAME);
    		buff[strlen(TOWER_NAME)-1] = tty[3];
    		tty = buff;
  	}  

	// Set debugging if RCXCOMM_DEBUG=Y 

	comm_debug = getenv("RCXCOMM_DEBUG");
	
	if (comm_debug != NULL && strcmp(comm_debug,"Y") == 0)
		rcx_set_debug(1);
	
	return __rcx_init(tty, is_fast);
}

void rcx_close(FILEDESCR fd)
{
	__rcx_close(fd);
	return;
}

int rcx_wakeup_tower (FILEDESCR fd, int timeout)
{
	char msg[] = { 0x10, 0xfe, 0x10, 0xfe };
	char keepalive = 0xff;
	char buf[BUFFERSIZE];
	timeval_t timer;
	int count = 0;
	int len;
	
	// First, I send a KeepAlive Byte to settle IR Tower...
	rcx_write(fd, &keepalive, 1);
	usleep(20000);
	rcx_flush(fd);
	
	timer_reset(&timer);
	
	do {
		if (__comm_debug) {
			printf("writelen = %d\n", sizeof(msg));
			hexdump("W", msg, sizeof(msg));
		}
		if (rcx_write(fd, msg, sizeof(msg)) != sizeof(msg)) {
			rcx_perror("write");
			return RCX_WRITE_FAIL;
		}
		count += len = rcx_read(fd, buf, BUFFERSIZE, 50);
		if (len == sizeof(msg) && !memcmp(buf, msg, sizeof(msg)))
			return RCX_OK; /* success */
		if (__comm_debug) {
			printf("recvlen = %d\n", len);
			hexdump("R", buf, len);
		}
		rcx_flush(fd);
	} while (timer_read(&timer) < (float)timeout / 1000.0f);
	
	if (!count)
		return RCX_NO_TOWER; /* tower not responding */
	else
		return RCX_BAD_LINK; /* bad link */
	
}


/* Hexdump routine */

#define LINE_SIZE   16
#define GROUP_SIZE  4
#define UNPRINTABLE '.'

void hexdump(char *prefix, void *buf, int len)
{
	unsigned char *b = (unsigned char *)buf;
	int i, j, w;

	for (i = 0; i < len; i += w) {
		w = len - i;
		if (w > LINE_SIZE)
			w = LINE_SIZE;
		if (prefix)
			printf("%s ", prefix);
		printf("%04x: ", i);
		for (j = 0; j < w; j++, b++) {
			printf("%02x ", *b);
			if ((j + 1) % GROUP_SIZE == 0)
				putchar(' ');
		}
		putchar('\n');
	}
}


int rcx_read (FILEDESCR fd, void *buf, int maxlen, int timeout)
{
	return __rcx_read(fd, buf, maxlen, timeout);
}

int rcx_write(FILEDESCR fd, const void *buf, size_t len) 
{
	return  __rcx_write(fd, buf, len);
}


int rcx_send (FILEDESCR fd, void *buf, int len, int use_comp)
{
	char *bufp = (char *)buf;
	char buflen = len;
	char msg[BUFFERSIZE];
	char echo[BUFFERSIZE];
	int msglen, echolen;
	int sum;
	int result = 0;

	/* Encode message */

	msglen = 0;
	sum = 0;

	if (use_comp) {
		msg[msglen++] = 0x55;
		msg[msglen++] = 0xff;
		msg[msglen++] = 0x00;
		while (buflen--) {
			msg[msglen++] = *bufp;
			msg[msglen++] = (~*bufp) & 0xff;
			sum += *bufp++;
		}
		msg[msglen++] = sum;
		msg[msglen++] = ~sum;
	}
	else {
		msg[msglen++] = 0xff;
		while (buflen--) {
			msg[msglen++] = *bufp;
			sum += *bufp++;
		}
		msg[msglen++] = sum;
	}

	/* Send message */

	if (rcx_write(fd, msg, msglen) != msglen) {
		rcx_perror("write");
		return RCX_WRITE_FAIL;
	}

	/* Receive echo */

	if ( usb_flag == 0 ) {	// usb ir tower dos not echo!!
		echolen = rcx_read(fd, echo, msglen, 100);

		if (__comm_debug) {
			printf("msglen = %d, echolen = %d\n", msglen, echolen);
			hexdump("C", echo, echolen);
		}

		/* Check echo */
		/* Ignore data, since rcx might send ack even if echo data is wrong */

		if (echolen != msglen /* || memcmp(echo, msg, msglen) */ ) {
			/* Flush connection if echo is bad */
			rcx_flush(fd);
			return RCX_BAD_ECHO;
		}
	}

	return len;
}

int rcx_recv (FILEDESCR fd, void *buf, int maxlen, int timeout, int use_comp)
{
	char *bufp = (char *)buf;
	unsigned char msg[BUFFERSIZE];
	int msglen;
	int sum;
	int pos;
	int len;
	int expected;
	int result = 0;

	/* Receive message */

	if (use_comp) {
		expected = maxlen * 2 + 3 + 2;
	} else {
		expected = BUFFERSIZE;
	}

	msglen = rcx_read(fd, msg, expected, timeout); 
 
	if (__comm_debug) {
		printf("maxlen = %d\n", maxlen);
		printf("recvlen = %d\n", msglen);
		hexdump("R", msg, msglen);
	}

	/* Check for message */

	if (!msglen)
		return RCX_NO_RESPONSE;

	/* Verify message */

	if (use_comp) {
		if (msglen < 5 || (msglen - 3) % 2 != 0)
			return RCX_BAD_RESPONSE;

		if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
			return RCX_BAD_RESPONSE;

		for (sum = 0, len = 0, pos = 3; pos < msglen - 2; pos += 2) {
			if (msg[pos] != ((~msg[pos+1]) & 0xff))
				return RCX_BAD_RESPONSE;
			sum += msg[pos];
			if (len < maxlen)
				bufp[len++] = msg[pos];
		}

		if (msg[pos] != ((~msg[pos+1]) & 0xff))
			return RCX_BAD_RESPONSE;

		if (msg[pos] != (sum & 0xff))
			return RCX_BAD_RESPONSE;

		if (__comm_debug) {
			printf("len = %d\n", len);
			hexdump("R", bufp, len);
		}

		/* Success */
		return len;
	}
	else {
		if (msglen < 4)
			return RCX_BAD_RESPONSE;

		if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
			return RCX_BAD_RESPONSE;

		for (sum = 0, len = 0, pos = 3; pos < msglen - 1; pos++) {
			sum += msg[pos];
			if (len < maxlen)
				bufp[len++] = msg[pos];
		}

		/* Return success if checksum matches */
		if (msg[pos] == (sum & 0xff))
			return len;

		/* Failed.  Possibly a 0xff byte queued message? (legos unlock firmware) */
		for (sum = 0, len = 0, pos = 3; pos < msglen - 2; pos++) {
			sum += msg[pos];
			if (len < maxlen)
				bufp[len++] = msg[pos];
		}

		/* Return success if checksum matches */
		if (msg[pos] == (sum & 0xff))
			return len;

		/* Failed.  Possibly a long message? */
		/* Long message if opcode is complemented and checksum okay */
		/* If long message, checksum does not include opcode complement */
		for (sum = 0, len = 0, pos = 3; pos < msglen - 1; pos++) {
			if (pos == 4) {
				if (msg[3] != ((~msg[4]) & 0xff))
					return RCX_BAD_RESPONSE;
			}
			else {
				sum += msg[pos];
				if (len < maxlen)
					bufp[len++] = msg[pos];
			}
		}

		if (msg[pos] != (sum & 0xff))
			return RCX_BAD_RESPONSE;

		/* Success */
		return len;
	}
}

int rcx_sendrecv (FILEDESCR fd, void *send, int slen, void *recv, int rlen,
		  int timeout, int retries, int use_comp)
{
	int status = 0;

	if (__comm_debug) printf("sendrecv %d:\n", slen);

	while (retries--) {
		if ((status = rcx_send(fd, send, slen, use_comp)) < 0) {
			if (__comm_debug) printf("send status = %s\n", rcx_strerror(status));
			continue;
		}
		if ((status = rcx_recv(fd, recv, rlen, timeout, use_comp)) < 0) {
			if (__comm_debug) printf("recv status = %s\n", rcx_strerror(status));
			continue;
		}
		break;
	}

	if (__comm_debug) {
		if (status > 0)
			printf("status = %s\n", rcx_strerror(0));
		else
			printf("status = %s\n", rcx_strerror(status));
	}

	return status;
}

int rcx_is_alive (FILEDESCR fd, int use_comp)
{
	unsigned char send[1] = { 0x10 };
	unsigned char recv[1];

	return (rcx_sendrecv(fd, send, 1, recv, 1, 50, 5, use_comp) == 1);
}

char *rcx_strerror (int error)
{
	switch (error) {
	case RCX_OK: return "no error";
	case RCX_NO_TOWER: return "tower not responding";
	case RCX_BAD_LINK: return "bad ir link";
	case RCX_BAD_ECHO: return "bad ir echo";
	case RCX_NO_RESPONSE: return "no response from rcx";
	case RCX_BAD_RESPONSE: return "bad response from rcx";
	case RCX_WRITE_FAIL: return "write failure";
	case RCX_READ_FAIL: return "read failure";
	case RCX_OPEN_FAIL: return "open failure";
	case RCX_INTERNAL_ERR: return "internal error";
	case RCX_ALREADY_CLOSED: return "already closed";
	case RCX_ALREADY_OPEN: return "already open";
	case RCX_NOT_OPEN: return "not open";
	case RCX_TIMED_OUT: return "operation timed out";
	default: return "unknown error";
	}
}

