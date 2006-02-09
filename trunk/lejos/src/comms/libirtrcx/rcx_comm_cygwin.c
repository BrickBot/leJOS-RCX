/*
 *  rcx_comm_cygwin.c
 *
 *  RCX communication routines for cygwin.
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
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>
#include <windows.h>

#include "rcx_comm_cygwin.h"

#define USB_TOWER_NAME "\\\\.\\LEGOTOWER1"

typedef struct
{
	char symbolicName[32];
	char deviceName[32];
	FILEDESCR fileHandle;
	int usb;
	int fast;
} Port;


#include "rcx_comm_cygwin.h"
#include "rcx_comm_os.h"
/*
#include "rcx_comm.c"
*/

extern int __comm_debug;

/*
 prototypes for some internal methods
*/

/* Set port parameters */
void __rcx_open_setDevice(Port* port, char* symbolicName, int is_fast);

/* Set serial port parameters.
Returns if success. */
int __rcx_open_setSerialPortParameters (Port* port);

/* Has timeout expired? */
int __rcx_read_isTimedOut (struct timeval *begin, int timeout_ms);

/* Read from USB port */
int __rcx_read_usb (rcx_dev_t *port, void* buf, int maxlen, int timeout_ms);

/* Read from serial port */
int __rcx_read_serial (void* port, void *buf, int maxlen, int timeout_ms);

void gettimeofday(struct timeval *tv, void *tzp);

/*
implementation
*/

void __rcx_perror(char *str) 
{
	if (__comm_debug) printf("Error %lu: %s\n", (unsigned long) GetLastError(), str);
}

int __rcx_read(rcx_dev_t *port, void *buffer, int max_len, int timeout_ms) {
	if (port->usb) 
	  return __rcx_read_usb (port, buffer, max_len, timeout_ms);
	 else
	  return __rcx_read_serial (port, buffer, max_len, timeout_ms);
}

int __rcx_read_isTimedOut (struct timeval *begin, int timeout_ms)
{
   struct timeval now;
   gettimeofday(&now, 0);

   int elapsed_ms = (now.tv_sec - begin->tv_sec) * 1000 + (now.tv_usec - begin->tv_usec) / 1000;
	if (__comm_debug) printf("elapsed = %d\n", elapsed_ms);
	if (__comm_debug) printf("timeout = %d\n", timeout_ms);
   return elapsed_ms >= timeout_ms;
}

int __rcx_read_usb (rcx_dev_t *port, void* buf, int maxlen, int timeout_ms)
{
	char* bufp = (char*) buf;
	int len = 0;

   struct timeval timebegin; 
   DWORD count = 0;

   /* try to read first bytes for timeout_ms milliseconds */
   gettimeofday(&timebegin, 0);
	do 
	{
      count = 0;
/*      	if (__comm_debug) printf("trying to read file handle: %s\n", ((Port*) port)->fileHandle);*/
		if (ReadFile(port->fd, bufp, maxlen, &count, NULL) == FALSE)
		{
			if (__comm_debug) printf("usb mode: read error %lu\n", (unsigned long) GetLastError());
			__rcx_perror("ReadFile");
			return RCX_READ_FAIL;
		}
      if (__comm_debug) printf("usb mode: read %d\n", count);
		len = count;
	}
	while (count == 0 && !__rcx_read_isTimedOut(&timebegin, timeout_ms));

	/* try to read following bytes (if any) with shorter timeout */
   if (count > 0 && len < maxlen)
   {
		do 
		{
			if (count > 0)
			{
				/* reset timer */
            gettimeofday(&timebegin, 0);
			}

			count = 0;
			if (ReadFile(((Port*) port)->fileHandle, &bufp[len], maxlen-len, &count, NULL) == FALSE)
			{
				if (__comm_debug) printf("usb mode: read error %lu\n", (unsigned long) GetLastError());
				__rcx_perror("ReadFile");
				return RCX_READ_FAIL;
			}
	      if (__comm_debug) printf("usb mode: read %d\n", count);
			
			len += count;
		}
		while (len < maxlen && (count > 0 || !__rcx_read_isTimedOut(&timebegin, 10)));
   }

	return len;
}

int __rcx_read_serial (void* port, void* buf, int maxlen, int timeout_ms)
{
	char* bufp = (char*) buf;
	int len = 0;

	/* Set the time-out parameters for all read and write operations on the port */
	COMMTIMEOUTS CommTimeouts;
	GetCommTimeouts (((Port*) port)->fileHandle, &CommTimeouts);
	CommTimeouts.ReadIntervalTimeout = MAXDWORD;
	CommTimeouts.ReadTotalTimeoutMultiplier = 0;
	CommTimeouts.ReadTotalTimeoutConstant = timeout_ms;
	CommTimeouts.WriteTotalTimeoutMultiplier = 10;
	CommTimeouts.WriteTotalTimeoutConstant = 1000;
	SetCommTimeouts(((Port*) port)->fileHandle, &CommTimeouts);

	while (len < maxlen) 
	{
		DWORD count = 0;
		if (ReadFile(((Port*) port)->fileHandle, &bufp[len], maxlen - len, &count, NULL) == FALSE) 
		{
			if (__comm_debug) printf("serial mode: read error %lu\n", (unsigned long) GetLastError());
			__rcx_perror("ReadFile");
			return RCX_READ_FAIL;
		}
      if (__comm_debug) printf("serial mode: read %d\n", count);

		if (count == 0) 
		{
			break;
		}

		len += count;
	}

	return len;
}

int __rcx_write(rcx_dev_t *port, const void *buffer, size_t len) {
	DWORD written = 0;
	if (WriteFile(port->fd, buffer, len, &written, NULL) == 0)
	{
		return RCX_WRITE_FAIL;
	}
	FlushFileBuffers(port->fd);
	
	return written;
}

void __rcx_purge(rcx_dev_t *port) {
	PurgeComm(port->fd, PURGE_RXABORT | PURGE_RXCLEAR | PURGE_TXABORT | PURGE_TXCLEAR);
}

void __rcx_flush(rcx_dev_t *port) {
    FlushFileBuffers (port->fd);
}

rcx_dev_t *__rcx_open(char *tty, int is_fast) {
	if (__comm_debug) printf("tty = %s\n", tty);
	if (__comm_debug) printf("mode = %s\n", is_fast ? "fast" : "slow");

	int success = 1;

	Port *result = (Port *) malloc(sizeof(Port));
	__rcx_open_setDevice(result, tty, is_fast);
			
	result->fileHandle = CreateFile(
	  result->deviceName, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
	if (result->fileHandle == INVALID_HANDLE_VALUE) 
   {
		if (__comm_debug) printf("Error %lu: Opening %s\n", (unsigned long) GetLastError(), result->deviceName);
		success = 0;
	}
	else if (!result->usb)
	{
      /* Setup serial port*/
		success = __rcx_open_setSerialPortParameters(result);
	}
	
	if (!success)
	{
		free(result);
	}

	if (__comm_debug) printf("device = %s\n", result->deviceName);
	if (__comm_debug) printf("port type = %s\n", result->usb? "usb" : "serial");

	rcx_dev_t *opened = (rcx_dev_t *) malloc(sizeof(rcx_dev_t));
	opened->fd = result->fileHandle;
	opened->tty = result->deviceName;
	opened->usb = result->usb;
	opened->fast = result->fast;
  
	return opened;
}

void __rcx_open_setDevice (Port *port, char* symbolicName, int is_fast)
{
	strncpy(port->symbolicName, symbolicName, 32);
   port->symbolicName[31] = 0;
   
   int length = strlen(symbolicName);
	if (strncmp(symbolicName, "usb", 3) == 0 && length <= 4)
	{
		/* usb mode (does _not_ support doubled baud rate) */
      strncpy(port->deviceName, USB_TOWER_NAME, 32);
      if (length == 4)
      {
      	/* multiple usb tower mode */
      	port->deviceName[strlen(USB_TOWER_NAME) - 1] = symbolicName[3];
      }
      port->usb = 1;	
      port->fast = is_fast; /* 2x: no complements */
	}
	else
	{
		/* serial mode */
		strncpy(port->deviceName, symbolicName, 32);
		port->deviceName[31] = 0;
		port->usb = 0;	
      port->fast = is_fast; /* 4x: no complements, doubled baud rate */
	}
}

int __rcx_open_setSerialPortParameters (Port *port)
{
   DCB dcb;
	FillMemory(&dcb, sizeof(dcb), 0);

	if (!GetCommState(port->fileHandle, &dcb)) 
	{	
		/* Get current DCB */
		__rcx_perror("GetCommState");
		return 0;
	} 

	dcb.ByteSize = 8;
	dcb.Parity   = (port->fast ? 0 : 1); /* 0-4 = no, odd, even, mark, space */
	dcb.StopBits = 0; /* 0,1,2 = 1, 1.5, 2 */
	dcb.fBinary  = TRUE;
	dcb.fParity  = (port->fast ? FALSE : TRUE);
	dcb.fAbortOnError = FALSE;
	dcb.BaudRate = (port->fast ? CBR_4800 : CBR_2400);	/* Update DCB rate. */

	if (!SetCommState(port->fileHandle, &dcb)) 
	{
		/* Error in SetCommState. Possibly a problem with the communications
		port handle or a problem with the DCB structure itself */
		__rcx_perror("SetCommState");
		return 0;
	}
	
	return 1;
}

void __rcx_close (rcx_dev_t *port) {
	CloseHandle(port->fd);
	free(port);
}

void gettimeofday( struct timeval *tv, void *tzp) 
{
	SYSTEMTIME st;
	GetSystemTime(&st);
	tv->tv_sec = (st.wHour) * 3600 + (st.wMinute) * 60 + st.wSecond;
	tv->tv_usec = st.wMilliseconds * 1024;
}

void usleep(int x) {
  Sleep(x/1000);
}
