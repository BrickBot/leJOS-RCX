#ifndef RCX_COMM_OS_H
#define RCX_COMM_OS_H

#include "rcx_comm.h"

#if defined(__cplusplus)
extern "C" {
#endif

/* Structures */

struct _rcx_port {
	char *tty;
	FILEDESCR fd;
	int usb;
	int fast;
};

/* Open tower on specific port.
 * name: symbolic port name
 * fast: use fast mode?
 * Returns port handle.
 */
rcx_dev_t *__rcx_open(char *name, int fast);

/* Close tower.
 * port: port handle
 */
void __rcx_close (rcx_dev_t *port);

/* Read raw bytes.
 * port: port handle
 * buffer: buffer to read into
 * maxLength: maximum number of bytes to read
 * timeout_ms: timeout in ms
 * Returns number of read bytes or an error code.
 */
int __rcx_read(rcx_dev_t *port, void *buffer, int max_len, int timeout_ms);

/* Write raw bytes.
 * port: port handle
 * buffer: buffer to write from
 * length: number of bytes to write
 * Returns number of written bytes or an error code.
 */
int __rcx_write(rcx_dev_t *port, const void *buffer, size_t len);

/* Purge input buffers.
 * port: port handle
 */
void __rcx_purge(rcx_dev_t *port);

/* Flush output buffers.
 * port: port handle
 */
void __rcx_flush(rcx_dev_t *port);

/* Output an error message.
 * message: error message
 */
void __rcx_perror(char *message);

#if defined(__cplusplus)
}
#endif
    
#endif
