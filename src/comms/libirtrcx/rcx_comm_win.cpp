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
#include <windows.h>

#define USB_TOWER_NAME "\\\\.\\LEGOTOWER1"
#define DEFAULTTTY "usb"

typedef struct timeval timeval_t;

#include "rcx_comm.h"
#include "rcx_comm_win.h"

//
// some internal methods
//

// Set port parameters.
void __rcx_open_setDevice(Port* port, char* symbolicName, bool fast);

// Set serial port parameters.
// Returns if success.
bool __rcx_open_setSerialPortParameters (Port* port);

// Read from USB port.
int __rcx_read_usb (void* port, void* buf, int maxlen, int timeout_ms);

// Read from serial port.
int __rcx_read_serial (void* port, void *buf, int maxlen, int timeout_ms);

void gettimeofday(struct timeval *tv, void *tzp);

//
// attributes
//

extern bool __comm_debug;

//
// interface
//

void __rcx_perror(char *str) 
{
	if (__comm_debug) fprintf(stderr, "Error %lu: %s\n", (unsigned long) GetLastError(), str);
}

int __rcx_read (void* port, void *buf, int maxlen, int timeout)
{
	return ((Port*) port)->usb? 
	  __rcx_read_usb (port, buf, maxlen, timeout) :
	  __rcx_read_serial (port, buf, maxlen, timeout);
	  
}

int __rcx_read_usb (void* port, void* buf, int maxlen, int timeout_ms)
{
	timeout_ms = 1000;
	char* bufp = (char*) buf;
	int len = 0;

	while (len < maxlen) 
	{
		DWORD count = 0;
		
		struct timeval timebegin, now;
		gettimeofday(&timebegin, 0);
	
		while (count == 0) 
		{
			ReadFile(((Port*) port)->fileHandle, &bufp[len], maxlen - len, &count, NULL);
         if (__comm_debug) fprintf(stderr, "usb mode: read %d\n", count);
			gettimeofday(&now, 0);
			int elapsed_ms = (now.tv_sec - timebegin.tv_sec) * 1000 + (now.tv_usec - timebegin.tv_usec) / 1000;	
			if (elapsed_ms > timeout_ms)
			{
            if (__comm_debug) fprintf(stderr, "usb mode: read time out\n");
				break;
			}
		}
		
		if (count == 0) 
		{
			break;
		}
		
		len += count;
	}

	return len;
}

int __rcx_read_serial (void* port, void* buf, int maxlen, int timeout_ms)
{
	char* bufp = (char*) buf;
	int len = 0;

	// Set the time-out parameters for all read and write operations on the port.
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
			__rcx_perror("ReadFile");
			fprintf(stderr, "serial mode: error reading tty: %lu\n", (unsigned long) GetLastError());
			return RCX_READ_FAIL;
		}
      if (__comm_debug) fprintf(stderr, "serial mode: read %d\n", count);

		if (count == 0) 
		{
			break;
		}

		len += count;
	}

	return len;
}

int __rcx_write(void* port, void* buf, int len) 
{
	DWORD written = 0;
	if (WriteFile(((Port*) port)->fileHandle, buf, len, &written, NULL) == 0)
	{
		return RCX_WRITE_FAIL;
	}
	FlushFileBuffers(((Port*) port)->fileHandle);
	
	return written;
}

void __rcx_purge(void* port)
{
	PurgeComm(((Port*) port)->fileHandle, PURGE_RXABORT | PURGE_RXCLEAR);
}

void __rcx_flush(void* port)
{
    FlushFileBuffers (((Port*) port)->fileHandle);
}

void* __rcx_open(char* tty, bool fast)
{
	if (__comm_debug) printf("tty = %s\n", tty);
	if (__comm_debug) printf("mode = %s\n", fast ? "fast" : "slow");

	bool success = true;

	Port* result = (Port*) malloc(sizeof(Port));
	__rcx_open_setDevice(result, tty, fast);
			
	result->fileHandle = CreateFile(
	  result->deviceName, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
	if (result->fileHandle == INVALID_HANDLE_VALUE) 
   {
		if (__comm_debug) printf("Error %lu: Opening %s\n", (unsigned long) GetLastError(), result->deviceName);
		success = false;
	}
	else if (result->usb == 0)
	{
      // Setup serial port
		success = __rcx_open_setSerialPortParameters(result);
	}
	
	if (!success)
	{
		free(result);
	}

	if (__comm_debug) printf("device = %s\n", result->deviceName);
	if (__comm_debug) printf("port type = %s\n", result->usb == 0 ? "serial" : "usb");
	if (__comm_debug) printf("mode = %s\n", result->fast? "fast" : "slow");

	return result;
}

void __rcx_open_setDevice (Port* port, char* symbolicName, bool fast)
{
	strncpy(port->symbolicName, symbolicName, 32);
   port->symbolicName[31] = 0;
   
   int length = strlen(symbolicName);
	if (strncmp(symbolicName, "usb", 3) == 0 && length <= 4)
	{
		// usb mode
      strncpy(port->deviceName, USB_TOWER_NAME, 32);
      if (length == 4)
      {
      	// multiple usb tower mode
      	port->deviceName[strlen(USB_TOWER_NAME) - 1] = symbolicName[3];
      }
      port->usb = true;	
      port->fast = false;	
	}
	else
	{
		// serial mode
		strncpy(port->deviceName, symbolicName, 32);
		port->deviceName[31] = 0;
		port->usb = false;	
      port->fast = fast;	
	}
}

bool __rcx_open_setSerialPortParameters (Port* port)
{
   DCB dcb;
	FillMemory(&dcb, sizeof(dcb), 0);

	if (!GetCommState(port->fileHandle, &dcb)) 
	{	
		// Get current DCB
		__rcx_perror("GetCommState");
		return 0;
	} 

	dcb.ByteSize = 8;
	dcb.Parity   = (port->fast ? 0 : 1); // 0-4 = no, odd, even, mark, space
	dcb.StopBits = 0; // 0,1,2 = 1, 1.5, 2
	dcb.fBinary  = TRUE;
	dcb.fParity  = (port->fast ? FALSE : TRUE);
	dcb.fAbortOnError = FALSE;
	dcb.BaudRate = (port->fast ? CBR_4800 : CBR_2400);	// Update DCB rate.

	if (!SetCommState(port->fileHandle, &dcb)) 
	{
		// Error in SetCommState. Possibly a problem with the communications
		// port handle or a problem with the DCB structure itself.
		__rcx_perror("SetCommState");
		return false;
	}
	
	return true;
}

void __rcx_close(void* port)
{
	CloseHandle(((Port*) port)->fileHandle);
	free(port);
}

void gettimeofday(timeval_t *tv, void *tzp) 
{
	SYSTEMTIME st;
	GetSystemTime(&st);
	tv->tv_sec = (st.wHour) * 3600 + (st.wMinute) * 60 + st.wSecond;
	tv->tv_usec = st.wMilliseconds * 1024;
}
