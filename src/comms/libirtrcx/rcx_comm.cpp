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

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>

#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>

#include "rcx_comm.h"

// Debug mode flag.
bool __comm_debug = false;

//
// Timer routines
//

typedef struct timeval timeval_t;

#define tvupdate(tv)  gettimeofday(tv, NULL)
#define tvsec(tv)     ((tv)->tv_sec)
#define tvmsec(tv)    ((tv)->tv_usec) / 1000

void gettimeofday(timeval_t *tv, void *tzp);

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

//
// Prototypes for internal methods
//

// Receive packet in fast mode.
// port: port handle
// buf: buffer to read into
// maxlen: maximum number of bytes to read
// timeout_ms: timeout in ms
// Returns number of received bytes or an error code.
int rcxReceiveFast (void* port, void* buf, int maxlen, int timeout_ms);

// Receive packet in normal mode.
// port: port handle
// buf: buffer to read into
// maxlen: maximum number of bytes to read
// timeout_ms: timeout in ms
// Returns number of received bytes or an error code.
int rcxReceiveSlow (void* port, void* buf, int maxlen, int timeout_ms);

// Check if echo is correct.
// port: port handle
// send: buffer with send bytes
// sendLength: number of sent bytes
bool rcxCheckEcho (void* port, char* send, int sendLength);

// Reset port. (Clear input and output buffers)
// port: port handle
void rcxReset (void* port);

//
// getter functions
//

bool rcxIsUsb(void* port)
{
	return ((Port*) port)->usb;
}

bool rcxIsFast(void* port)
{
	return ((Port*) port)->fast;
}

//
// RCX functions
//

void* rcxOpen(char* portName, bool fast)
{
	return __rcx_open(portName, fast);
}

void rcxClose(void* port)
{
	__rcx_close(port);
}

int rcxWakeupTower (void* port, int timeout_ms)
{
	// wake up message
	char msg[] = { 0x10, 0xfe, 0x10, 0xfe };
	// keep alive message
	char keepalive[] = { 0xff };
	// receive buffer
	char buf[BUFFERSIZE];
	// timer
	timeval_t timer;
	// received bytes
	int count = 0;
	
	// First, I send a KeepAlive Byte to settle IR Tower...
	rcxReset(port);
	rcxWrite(port, &keepalive, 1);
	rcxReset(port);
	
	timerReset(&timer);
	do 
	{
      // write message		
		int written = rcxWrite(port, msg, sizeof(msg));
		if (written != sizeof(msg)) 
		{
			rcxPerror("write");
			return RCX_WRITE_FAIL;
		}
		
		// read response
		int read = rcxRead(port, buf, BUFFERSIZE, 50);
		count += read;
		if (__comm_debug) 
		{
			printf("read = %d\n", read);
			hexdump("R", buf, read);
		}

		// test response
		if (read == sizeof(msg) && !memcmp(buf, msg, sizeof(msg)))
		{
			return RCX_OK;
		}

	   rcxReset(port);
	} while (timerRead(&timer) < timeout_ms);
	
	return count == 0? RCX_NO_TOWER : RCX_BAD_LINK;	
}

int rcxRead (void* port, void* read, int readMaxLength, int timeout_ms)
{
	int result = __rcx_read(port, read, readMaxLength, timeout_ms);

	if (__comm_debug) 
	{
		hexdump("R", read, result);
	}

   return result;
}

int rcxWrite(void* port, void* write, int writeLength) 
{
	if (__comm_debug) hexdump("W", write, writeLength);

	return  __rcx_write(port, write, writeLength);
}

int rcxSend (void* port, void* send, int sendLength)
{
	char *bufp = (char*) send;
	int buflen = sendLength;

	if (__comm_debug) hexdump("S", send, sendLength);

	// Encode message
	char msg[BUFFERSIZE];
	int msglen = 0;
	int sum = 0;
	if (rcxIsFast(port)) 
	{
		msg[msglen++] = 0xff;
		while (sendLength--) 
		{
			msg[msglen++] = *bufp;
			sum += *bufp++;
		}
		msg[msglen++] = sum;
	}
	else 
	{
		msg[msglen++] = 0x55;
		msg[msglen++] = 0xff;
		msg[msglen++] = 0x00;
		while (sendLength--) 
		{
			msg[msglen++] = *bufp;
			msg[msglen++] = (~*bufp) & 0xff;
			sum += *bufp++;
		}
		msg[msglen++] = sum;
		msg[msglen++] = ~sum;
	}

	// Send message
	rcxPurge(port);
	int written = rcxWrite(port, msg, msglen);
	if (written != msglen) 
	{
      if (__comm_debug) printf("wrong number of bytes sent\n");
		rcxPerror("write");
		return RCX_WRITE_FAIL;
	}

	// Check echo
   // USB tower does not echo!
   rcxFlush(port);
	if (!rcxIsUsb(port) && !rcxCheckEcho(port, msg, msglen))
	{
      if (__comm_debug) printf("wrong echo\n");
		return RCX_BAD_ECHO;
	}

	return buflen;
}

bool rcxCheckEcho (void* port, char* send, int sendLength)
{
	char echo[BUFFERSIZE];

	int read = rcxRead(port, echo, sendLength, 100);

	if (__comm_debug) 
	{
		hexdump("C", echo, read);
	}

	// Check echo
	// Ignore data, since rcx might send ack even if echo data is wrong
	bool result = read == sendLength /* && !memcmp(echo, send, sendLength) */? 1 : 0;
	if (!result) 
	{
		// Purge connection if echo is bad
		rcxPurge(port);
	}
	
	return result;
}

int rcxReceive (void* port, void* buf, int maxlen, int timeout_ms)
{
	rcxIsFast(port)?  rcxReceiveFast(port, buf, maxlen, timeout_ms) : rcxReceiveSlow(port, buf, maxlen, timeout_ms);
}

int rcxReceiveFast (void* port, void* buf, int maxlen, int timeout_ms)
{
	char *bufp = (char *)buf;
	unsigned char msg[BUFFERSIZE];
	int sum;
	int pos;
	int len;
	int result = 0;

	// Receive message
	int expected = 3 + maxlen + 1; // TODO correct? was BUFFERSIZE...
	int read = rcxRead(port, msg, expected, timeout_ms); 
 
	// Check for message
	if (read == 0)
	{	
		return RCX_NO_RESPONSE;
	}

	// Verify message
	if (read < 4)
	{
		if (__comm_debug) printf("response too short\n");
		return RCX_BAD_RESPONSE;
	}
	
	if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
	{
		if (__comm_debug) printf("wrong response header\n");
		return RCX_BAD_RESPONSE;
	}

	for (sum = 0, len = 0, pos = 3; pos < read - 1; pos++) 
	{
		sum += msg[pos];
		if (len < maxlen)
		{
			bufp[len++] = msg[pos];
		}
	}

	// Return success if checksum matches
	if (msg[pos] == (sum & 0xff))
	{
      if (__comm_debug) printf("normal message\n");
		if (__comm_debug) hexdump("R", bufp, len);
		return len;
	}
	
	// Failed. Possibly a 0xff byte queued message? (legos unlock firmware)
	for (sum = 0, len = 0, pos = 3; pos < read - 2; pos++) 
	{
		sum += msg[pos];
		if (len < maxlen)
		{
			bufp[len++] = msg[pos];
		}
	}

	// Return success if checksum matches
	if (msg[pos] == (sum & 0xff))
	{
      if (__comm_debug) printf("queued message\n");
		if (__comm_debug) hexdump("R", bufp, len);
		return len;
	}

	// Failed. Possibly a long message?
	/* Long message if opcode is complemented and checksum okay */
	/* If long message, checksum does not include opcode complement */
	for (sum = 0, len = 0, pos = 3; pos < read - 1; pos++) 
	{
		if (pos == 4) 
		{
			if (msg[3] != ((~msg[4]) & 0xff))
			{
				return RCX_BAD_RESPONSE;
			}
		}
		else 
		{
			sum += msg[pos];
			if (len < maxlen)
			{
				bufp[len++] = msg[pos];
			}
		}
	}

	// Return success if checksum matches
	if (msg[pos] == (sum & 0xff))
	{
      if (__comm_debug) printf("long message\n");
		if (__comm_debug) hexdump("R", bufp, len);
		return len;
	}

   if (__comm_debug) printf("bad message\n");
	return RCX_BAD_RESPONSE;
}

int rcxReceiveSlow (void* port, void* buf, int maxlen, int timeout_ms)
{
	char* bufp = (char*) buf;
	unsigned char msg[BUFFERSIZE];
	int sum;
	int pos;
	int len;
	int result = 0;

	// Receive message
	int expected = 3 + maxlen * 2 + 2;
	int read = rcxRead(port, msg, expected, timeout_ms); 
 
	// Check for message
	if (read == 0)
	{	
		return RCX_NO_RESPONSE;
	}

	// Verify message
	if (read < 5)
	{
		if (__comm_debug) printf("response too short\n");
		return RCX_BAD_RESPONSE;
	}

	if ((read - 3) % 2 != 0)
	{
		if (__comm_debug) printf("wrong response length\n");
		return RCX_BAD_RESPONSE;
	}

	if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
	{
		if (__comm_debug) printf("wrong response header\n");
		return RCX_BAD_RESPONSE;
	}

	for (sum = 0, len = 0, pos = 3; pos < read - 2; pos += 2) 
	{
		if (msg[pos] != ((~msg[pos+1]) & 0xff))
		{
         if (__comm_debug) printf("inverted byte is wrong\n");
			return RCX_BAD_RESPONSE;
		}
		sum += msg[pos];
		if (len < maxlen)
		{
			bufp[len++] = msg[pos];
		}
	}

	if (msg[pos] != ((~msg[pos+1]) & 0xff))
	{
      if (__comm_debug) printf("inverted checksum is wrong\n");
		return RCX_BAD_RESPONSE;
   }

	if (msg[pos] != (sum & 0xff))
	{
      if (__comm_debug) printf("message checksum is wrong\n");
		return RCX_BAD_RESPONSE;
	}

	if (__comm_debug) hexdump("R", buf, len);

	// Success
	return len;
}

int rcxSendReceive (void* port, void* send, int sendLength, 
  void* receive, int receiveLength, int timeout_ms, int retries)
{
	int status = 0;

	if (__comm_debug) printf("sendrecv %d:\n", sendLength);

	while (retries--) 
	{
		status = rcxSend(port, send, sendLength);
		if (status < 0) 
		{
			if (__comm_debug) printf("send status = %s\n", rcxStrerror(status));
			// retry
			continue;
		}
		
		status = rcxReceive(port, receive, receiveLength, timeout_ms);
		if (status < 0) 
		{
			if (__comm_debug) printf("receive status = %s\n", rcxStrerror(status));
			// retry
			continue;
		}

		// success
		break;
	}

	if (__comm_debug) printf("status = %s\n", rcxStrerror(status > 0? 0 : status));

	return status;
}

void rcxReset (void* port)
{
	char buf[BUFFERSIZE];
	
	// clear input and output buffers
	rcxPurge(port);
	// try to flush remaining bytes of output buffer
	rcxFlush(port);
	usleep(20000);
	// clear input and output buffers
	rcxPurge(port);
	// try to read remaining bytes from input buffer
	rcxRead(port, buf, BUFFERSIZE, 1);
}

void rcxPurge (void* port)
{
	__rcx_purge(port);
}

void rcxFlush(void* port)
{
	__rcx_flush(port);
}

bool rcxIsAlive (void* port)
{
	unsigned char send[1] = { 0x10 };
	unsigned char recv[1];

	rcxReset(port);
	int read = rcxSendReceive(port, send, 1, recv, 1, 50, 5);

	return read == 1;
}

void rcxPerror(char *str) 
{
	__rcx_perror(str);
}

//
// error handling
//

char* rcxStrerror (int error)
{
	switch (error) 
	{
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

//
// debug stuff
//

void rcxSetDebug(bool debug)
{
	__comm_debug = debug;
}

bool rcIsDebug() 
{
	return __comm_debug;
}

// Hexdump routine.
#define LINE_SIZE   16
#define GROUP_SIZE  4
#define UNPRINTABLE '.'
void hexdump(char* prefix, void* buf, int len)
{
	unsigned char *b = (unsigned char *)buf;
	int i, j, w;

	for (i = 0; i < len; i += w) 
	{
		w = len - i;
		if (w > LINE_SIZE)
			w = LINE_SIZE;
		if (prefix)
			printf("%s ", prefix);
		printf("%04x: ", i);
		for (j = 0; j < w; j++, b++) 
		{
			printf("%02x ", *b);
			if ((j + 1) % GROUP_SIZE == 0)
				putchar(' ');
		}
		putchar('\n');
	}
}
