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
 */

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>

#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>
#include <sys/time.h>

#include "rcx_comm_os.h"

#include "rcx_comm.h"

/* Debug mode flag. */
int __comm_debug = 0;

/* Timer routines */

typedef struct timeval timeval_t;

#define tvupdate(tv)  gettimeofday(tv, NULL)
#define tvsec(tv)     ((tv)->tv_sec)
#define tvmsec(tv)    ((tv)->tv_usec) / 1000


static void timerReset(timeval_t *timer)
{
	tvupdate(timer);
}

static int timerRead(timeval_t *timer)
{
	timeval_t now;
	tvupdate(&now);
	return (tvsec(&now) - tvsec(timer)) * 1000 + (tvmsec(&now) - tvmsec(timer));
}


/* Prototypes for internal methods */

/* Receive packet in fast mode.
 * port: port handle
 * buf: buffer to read into
 * maxlen: maximum number of bytes to read
 * timeout_ms: timeout in ms
 * Returns number of received bytes or an error code.
 */
static int rcx_receive_fast(rcx_dev_t *port, void *buf, int maxlen, int timeout_ms);

/* Receive packet in normal mode.
 * port: port handle
 * buf: buffer to read into
 * maxlen: maximum number of bytes to read
 * timeout_ms: timeout in ms
 * Returns number of received bytes or an error code.
 */
static int rcx_receive_slow(rcx_dev_t *port, void *buf, int maxlen, int timeout_ms);

/* Encode bytes to correct message format.
 * port: port handle
 * send: buffer with bytes to send as message
 * sendLength: number of bytes to send
 * message: buffer to which the message will be encoded
 * Returns length of encoded message
 */
static int rcx_encode_message(rcx_dev_t *port, char* send, int sendLength, char* message);

/* Check if echo is correct.
 * port: port handle
 * send: buffer with send bytes
 * sendLength: number of sent bytes
 */
static int rcx_check_echo(rcx_dev_t *port, char* send, int sendLength);

/* Reset port. (Clear input and output buffers)
 * port: port handle
 */
static void rcx_reset(rcx_dev_t *port);



/* getter functions */
int rcx_is_usb(rcx_dev_t *port)
{
	return port->usb;
}

int rcx_is_fast(rcx_dev_t *port)
{
	return port->fast;
}


/* RCX functions */

rcx_dev_t *rcx_open(char *port_name, int fast)
{
	return __rcx_open(port_name, fast);
}

void rcx_close(rcx_dev_t *port)
{
	__rcx_close(port);
}

int rcx_wakeup_tower(rcx_dev_t *port, int timeout_ms)
{
	/* wake up message */
	char msg[] = { 0x10, 0xfe, 0x10, 0xfe };
	/* keep alive message */
	char keepalive[] = { 0xff };
	/* receive buffer */
	char buf[BUFFERSIZE];
	/* timer */
	timeval_t timer;
	/* received bytes */
	int count = 0;
	
	/* First, I send a KeepAlive Byte to settle IR Tower... */
	rcx_reset(port);
	rcx_write(port, &keepalive, 1);
	rcx_reset(port);
	
	timerReset(&timer);
	do {
		/* write message */
		int written = rcx_write(port, msg, sizeof(msg));
		if (written != sizeof(msg)) {
			rcx_perror("write");
			return RCX_WRITE_FAIL;
		}
		
		/* read response */
		int read = rcx_read(port, buf, BUFFERSIZE, 50);
		count += read;

		if (__comm_debug) printf("read = %d\n", read);
		if (__comm_debug) hexdump("R", buf, read);

		/* test response */
		if (read == sizeof(msg) && !memcmp(buf, msg, sizeof(msg))) {
			return RCX_OK;
		}

		rcx_reset(port);
	} while (timerRead(&timer) < timeout_ms);
	
	return count == 0 ? RCX_NO_TOWER : RCX_BAD_LINK;	
}

int rcx_read(rcx_dev_t *port, void *read, int readMaxLength, int timeout_ms)
{
	int result = __rcx_read(port, read, readMaxLength, timeout_ms);

	if (__comm_debug) 
	{
		hexdump("R", read, result);
	}

   return result;
}

int rcx_write(rcx_dev_t *port, void *write, int write_len) 
{
	if (__comm_debug) hexdump("W", write, write_len);

	return  __rcx_write(port, write, write_len);
}

int rcx_send(rcx_dev_t *port, void *send, int send_len)
{
	int written = 0;

	if (__comm_debug) hexdump("S", send, send_len);

	/* Encode message */
	char message[BUFFERSIZE];
	int message_len = rcx_encode_message(port, (char*)send, send_len,
					     message);

	/* Send message */
	rcx_purge(port);
	written = rcx_write(port, message, message_len);
	if (written < 0) {
		/* Pass error through */
		return written;
	}
	else if (written != message_len) {
		if (__comm_debug) printf("wrong number of bytes sent\n");
		rcx_perror("write");
		return RCX_WRITE_FAIL;
	}

	/* Check echo */
	/* USB tower does not echo! */
	rcx_flush(port);
	if (!rcx_is_usb(port) && !rcx_check_echo(port, message, message_len)) {
		if (__comm_debug) printf("wrong echo\n");
		return RCX_BAD_ECHO;
	}
	return send_len;
}

int rcx_encode_message(rcx_dev_t *port, char* send, int send_len, char* message)
{
	int is_fast = rcx_is_fast(port);
	int len = 0;
	int sum = 0;

	message[len++] = 0x55;
	message[len++] = 0xff;
	message[len++] = 0x00;
	while (send_len--) {
		message[len++] = *send;
		if (!is_fast) {
			message[len++] = (~*send) & 0xff;
		}
		sum += *send++;
	}
	message[len++] = sum;
	if (!is_fast) {
		message[len++] = ~sum;
	}
	return len;
}

int rcx_check_echo(rcx_dev_t *port, char *send, int send_len)
{
	char echo[BUFFERSIZE];
	int result = 0;
	int read = rcx_read(port, echo, send_len, 100);

	if (__comm_debug) {
		hexdump("C", echo, read);
	}

	/* Check echo */
	/* Ignore data, since RCX might send ACK even if echo data is wrong. */
	result = read == send_len
		/* && !memcmp(echo, send, send_len) ? 1 : 0 */;
	if (!result) {
		/* Purge connection if echo is bad */
		rcx_purge(port);
	}
	return result;
}

int rcx_receive(rcx_dev_t *port, void *buf, int maxlen, int timeout_ms)
{
	return rcx_is_fast(port)
		? rcx_receive_fast(port, buf, maxlen, timeout_ms)
		: rcx_receive_slow(port, buf, maxlen, timeout_ms);
}

int rcx_receive_fast(rcx_dev_t *port, void *buf, int maxlen, int timeout_ms)
{
	char *bufp = (char *)buf;
	unsigned char msg[BUFFERSIZE];
	int sum;
	int pos;
	int len;

	/* Receive message */
	int expected = 3 + maxlen + 1; /* TODO correct? was BUFFERSIZE... */
	int read = rcx_read(port, msg, expected, timeout_ms); 
 
	/* Check for message */
	if (read == 0) {	
		return RCX_NO_RESPONSE;
	}

	/* Verify message */
	if (read < 4) {
		if (__comm_debug) printf("response too short\n");
		return RCX_BAD_RESPONSE;
	}
	
	if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)	{
		if (__comm_debug) printf("wrong response header\n");
		return RCX_BAD_RESPONSE;
	}

	for (sum = 0, len = 0, pos = 3; pos < read - 1; pos++) {
		sum += msg[pos];
		if (len < maxlen) {
			bufp[len++] = msg[pos];
		}
	}

	/* Return success if checksum matches */
	if (msg[pos] == (sum & 0xff))
	{
		if (__comm_debug) printf("normal message\n");
		if (__comm_debug) hexdump("R", bufp, len);
		return len;
	}
	
	/* Failed. Possibly a 0xff byte queued message? 
	 * (legos unlock firmware) */
	for (sum = 0, len = 0, pos = 3; pos < read - 2; pos++) 
	{
		sum += msg[pos];
		if (len < maxlen) {
			bufp[len++] = msg[pos];
		}
	}

	/* Return success if checksum matches */
	if (msg[pos] == (sum & 0xff)) {
		if (__comm_debug) printf("queued message\n");
		if (__comm_debug) hexdump("R", bufp, len);
		return len;
	}

	/* Failed. Possibly a long message? */
	/* Long message if opcode is complemented and checksum okay. */
	/* If long message, checksum does not include opcode complement. */
	for (sum = 0, len = 0, pos = 3; pos < read - 1; pos++) {
		if (pos == 4) {
			if (msg[3] != ((~msg[4]) & 0xff)) {
				return RCX_BAD_RESPONSE;
			}
		}
		else {
			sum += msg[pos];
			if (len < maxlen) {
				bufp[len++] = msg[pos];
			}
		}
	}

	/* Return success if checksum matches */
	if (msg[pos] == (sum & 0xff)) {
		if (__comm_debug) printf("long message\n");
		if (__comm_debug) hexdump("R", bufp, len);
		return len;
	}

	if (__comm_debug) printf("bad message\n");
	return RCX_BAD_RESPONSE;
}

int rcx_receive_slow(rcx_dev_t *port, void *buf, int maxlen, int timeout_ms)
{
	char *bufp = (char*)buf;
	unsigned char msg[BUFFERSIZE];
	int sum;
	int pos;
	int len;

	/* Receive message */
	int expected = 3 + maxlen * 2 + 2;
	int read = rcx_read(port, msg, expected, timeout_ms); 
 
	/* Check for message */
	if (read == 0) {	
		return RCX_NO_RESPONSE;
	}

	/* Verify message */
	if (read < 5) {
		if (__comm_debug) printf("response too short\n");
		return RCX_BAD_RESPONSE;
	}

	if ((read - 3) % 2 != 0) {
		if (__comm_debug) printf("wrong response length\n");
		return RCX_BAD_RESPONSE;
	}

	if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00) {
		if (__comm_debug) printf("wrong response header\n");
		return RCX_BAD_RESPONSE;
	}

	for (sum = 0, len = 0, pos = 3; pos < read - 2; pos += 2) {
		if (msg[pos] != ((~msg[pos+1]) & 0xff)) {
			if (__comm_debug) printf("inverted byte is wrong\n");
			return RCX_BAD_RESPONSE;
		}
		sum += msg[pos];
		if (len < maxlen) {
			bufp[len++] = msg[pos];
		}
	}

	if (msg[pos] != ((~msg[pos+1]) & 0xff)) {
		if (__comm_debug) printf("inverted checksum is wrong\n");
		return RCX_BAD_RESPONSE;
	}

	if (msg[pos] != (sum & 0xff)) {
		if (__comm_debug) printf("message checksum is wrong\n");
		return RCX_BAD_RESPONSE;
	}

	if (__comm_debug) {
		hexdump("R", buf, len);
	}
	return len;
}

int rcx_send_receive(rcx_dev_t *port, void *send, int send_len, 
		     void *receive, int receive_len,
		     int timeout_ms, int retries)
{
	int status = 0;

	if (__comm_debug) printf("sendrecv %d:\n", send_len);

	while (retries--) {
		status = rcx_send(port, send, send_len);
		if (status < 0) {
			if (__comm_debug) printf("send status = %s\n", rcx_strerror(status));
			/* retry */
			continue;
		}
		
		status = rcx_receive(port, receive, receive_len, timeout_ms);
		if (status < 0) {
			if (__comm_debug) printf("receive status = %s\n", rcx_strerror(status));
			/* retry */
			continue;
		}

		/* success */
		break;
	}

	if (__comm_debug) {
		printf("status = %s\n", rcx_strerror(status > 0 ? 0 : status));
	}
	return status;
}

void rcx_reset(rcx_dev_t *port)
{
	char buf[BUFFERSIZE];
	
	/* clear input and output buffers */
	rcx_purge(port);
	/* try to flush remaining bytes of output buffer */
	rcx_flush(port);
	usleep(20000);
	/* clear input and output buffers */
	rcx_purge(port);
	/* try to read remaining bytes from input buffer */
	rcx_read(port, buf, BUFFERSIZE, 1);
}

void rcx_purge(rcx_dev_t *port)
{
	__rcx_purge(port);
}

void rcx_flush(rcx_dev_t *port)
{
	__rcx_flush(port);
}

int rcx_is_alive(rcx_dev_t *port)
{
	unsigned char send[1] = { 0x10 };
	unsigned char recv[1];

	rcx_reset(port);
	int read = rcx_send_receive(port, send, 1, recv, 1, 50, 5);

	return read == 1;
}

void rcx_perror(char *str) 
{
	__rcx_perror(str);
}

/* error handling */

char *rcx_strerror(int error)
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


/* debugging */

void rcx_set_debug(int debug)
{
	__comm_debug = debug;
}

int rcx_is_debug() 
{
	return __comm_debug;
}

/* Hexdump routine. */
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
