/*
 *  rcx_comm.h
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
 */

#ifndef RCX_COMM_H_INCLUDED
#define RCX_COMM_H_INCLUDED

#define BUFFERSIZE  4096

//
// Error codes
//

#define RCX_OK               0
#define RCX_NO_TOWER        -1
#define RCX_BAD_LINK        -2
#define RCX_BAD_ECHO        -3
#define RCX_NO_RESPONSE     -4
#define RCX_BAD_RESPONSE    -5
#define RCX_WRITE_FAIL      -6
#define RCX_READ_FAIL       -7
#define RCX_OPEN_FAIL       -8
#define RCX_INTERNAL_ERR    -9
#define RCX_ALREADY_CLOSED -10
#define RCX_ALREADY_OPEN   -11
#define RCX_NOT_OPEN       -12
#define RCX_TIMED_OUT      -13

#define RCX_NOT_IMPL -256

//
// OS specific types
//

#if defined(_WIN32) || defined(__CYGWIN32__)
  #include "rcx_comm_win.h"
#elif defined(__APPLE__)
  #include "rcx_comm_osx.h"
#else
  #include "rcx_comm_linux.h"
#endif

//
// Structures
//

typedef struct
{
	char symbolicName[32];
	char deviceName[32];
	FILEDESCR fileHandle;
	int usb;
	int fast;
} Port;

//
// interface
//

/* Get a file descriptor for the named tty, exits with message on error */
// extern FILEDESCR rcx_init (char *tty, int is_fast);

/* Close a file descriptor allocated by rcx_init */
// extern void rcx_close (FILEDESCR fd);

/* Try to wakeup the tower for timeout ms, returns error code */
// extern int rcx_wakeup_tower (FILEDESCR fd, int timeout);

/* Try to send a message, returns error code */
/* Set use_comp=1 to send complements, use_comp=0 to suppress them */
// extern int rcx_send (FILEDESCR fd, void *buf, int len, int use_comp);

/* Try to receive a message, returns error code */
/* Set use_comp=1 to expect complements */
/* Waits for timeout ms before detecting end of response */
// extern int rcx_recv (FILEDESCR fd, void *buf, int maxlen, int timeout, int use_comp);

/* Try to send a message and receive its response, returns error code */
/* Set use_comp=1 to send and receive complements, use_comp=0 otherwise */
/* Waits for timeout ms before detecting end of response */
// extern int rcx_sendrecv (FILEDESCR fd, void *send, int slen, void *recv, int rlen, int timeout, int retries, int use_comp);

/* Test whether or not the rcx is alive, returns 1=yes, 0=no */
/* Set use_comp=1 to send complements, use_comp=0 to suppress them */
// extern int rcx_is_alive (FILEDESCR fd, int use_comp);


#endif /* RCX_COMM_H_INCLUDED */

//
// getter functions
//

// Is tower attached to an usb port?
int rcxIsUsb(void* port);

// Is tower set to fast mode?
int rcxIsFast(void* port);

//
// RCX functions
//

// Open tower on specific port.
// Returns port handle.
void* rcxOpen(char* port, int isFast);

// Close tower.
void rcxClose(void* port);

// Wake up tower / RCX.
// Returns error code.
int rcxWakeupTower (void* port, int timeout);

// Read bytes.
// Returns number of read bytes.
int rcxRead (void* port, void *buf, int maxlen, int timeout);

// Write bytes.
// Returns number of written bytes.
int rcxWrite(void* port, void* buf, int len);

// Send packet.
// Returns number of sent bytes or an error code.
int rcxSend (void* port, void *buf, int len);

// Receive packet.
// Returns number of read bytes or an error code.
int rcxReceive (void* port, void *buf, int maxlen, int timeout);

// Receive packet in fast mode.
// Returns number of read bytes or an error code.
int rcxReceiveFast (void* port, void *buf, int maxlen, int timeout);

// Receive packet.
// Returns number of read bytes or an error code.
int rcxReceiveSlow (void* port, void *buf, int maxlen, int timeout);

// Send a packet an receive a response.
int rcxSendReceive (void* port, void* send, int slen, void *recv, int rlen, int timeout, int retries);
		  
// Is RCX alive?
int rcxIsAlive (void* port);

// Flush buffers.
void rcxFlush(void* port);

// ???
void rcxPerror(char *str) ;

//
// error handling
//

// Get string representation for error code.
char* rcxStrerror (int error);

//
// debug stuff
//

// Set debug mode.
void rcxSetDebug(int debug);

// Is librcx in debug mode?
int rcIsDebug();

// Hexdump routine.
void hexdump(char *prefix, void *buf, int len);
