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

#ifndef FILEDESCR
#define FILEDESCR	int
#endif

typedef struct _rcx_port rcx_dev_t;


#define BUFFERSIZE  4096

/* Error codes */

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

/* getter functions */

/* Is tower attached to an usb port?
 * port: port handle
 */
int rcx_is_usb(rcx_dev_t *port);

/* Is tower set to fast mode?
 * port: port handle
 */
int rcx_is_fast(rcx_dev_t *port);

/* RCX functions */

/* Open tower on specific port.
 * port: port handle
 * fast: use fast mode?
 * Returns port handle.
 */
rcx_dev_t *rcx_open(char *port, int fast);

/* Close tower.
 * port: port handle
 */
void rcx_close(rcx_dev_t *port);

/* Wake up tower / RCX.
 * port: port handle
 * timeout_ms: timeout in ms
 * Returns error code.
 */
int rcx_wakeup_tower(rcx_dev_t *port, int timeout_ms);

/* Read raw bytes.
 * port: port handle
 * buf: buffer to read into
 * maxlen: maximum number of bytes to read
 * timeout_ms: timeout in ms
 * Returns number of read bytes or an error code.
 */
int rcx_read(rcx_dev_t *port, void *buf, int maxlen, int timeout_ms);

/* Write raw bytes.
 * port: port handle
 * buf: buffer to write from
 * len: number of bytes to write
 * Returns number of written bytes or an error code.
 */
int rcx_write(rcx_dev_t *port, void *buf, int len);

/* Send a packet.
 * port: port handle
 * buf: buffer to write from
 * len: number of bytes to write
 * Returns number of sent bytes or an error code.
 */
int rcx_send(rcx_dev_t *port, void *buf, int len);

/* Receive a packet.
 * port: port handle
 * buf: buffer to read into
 * maxlen: maximum number of bytes to read
 * timeout_ms: timeout in ms
 * Returns number of received bytes or an error code.
 */
int rcx_receive(rcx_dev_t *port, void *buf, int maxlen, int timeout_ms);

/* Send a packet and receive a response.
 * port: port handle
 * send: buffer to write from
 * slen: maximum number of bytes to write
 * recv: buffer to read into
 * rlen: maximum number of bytes to read
 * retries: number of retries
 * Returns number of received bytes or an error code.
 */
int rcx_send_receive(rcx_dev_t *port,
		     void *send, int slen,
		     void *recv, int rlen,
		     int timeout, int retries);

/* Clear input and output buffers.
 * port: port handle
 */
void rcx_purge(rcx_dev_t *port);

/* Flush output buffers.
 * port: port handle
 */
void rcx_flush(rcx_dev_t *port);

/* Is RCX alive?
 */
int rcx_is_alive (rcx_dev_t *port);

/* error handling */

/* Get string representation for error code.
 * error: error code as returned by above methods
 */
char *rcx_strerror (int error);

/* Print error message
 * message: error message
 */
void rcx_perror(char *message);


/* debuging */

/* Set debug mode.
 * debug: do debug output?
 */
void rcx_set_debug(int debug);

/* Is librcx in debug mode?
 */
int rcx_is_debug();

/* Hexdump routine.
 * prefix: prefix for each line
 * buf: buffer to dump
 * len: number of bytes to dump
 */
void hexdump(char *prefix, void *buf, int len);

#endif /* RCX_COMM_H_INCLUDED */
