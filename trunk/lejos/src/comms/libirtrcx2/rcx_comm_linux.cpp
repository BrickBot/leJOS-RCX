/*
 *  rcx_comm_linux.cpp
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
#include <errno.h>
#include <string.h>

#define USB_TOWER_NAME "/dev/usb/legousbtower0"

#include "rcx_comm_linux.h"
#include "rcx_comm.cpp"

//
// prototypes for some internal methods
//

// Set port parameters.
void __rcx_open_setDevice(Port* port, char* symbolicName, bool fast);

// Set serial port parameters.
// Returns if success.
bool __rcx_open_setSerialPortParameters (Port* port);

//
// implementation
//

void __rcx_perror(char* str) 
{
 	if (__comm_debug) perror(str);
}

int __rcx_read (void* port, void *buf, int maxlen, int timeout)
{
	char *bufp = (char*) buf;

	fd_set fds;

	struct timeval tv;

	long len = 0;
	while (len < maxlen) 
	{
		FD_ZERO(&fds);
		FD_SET(((Port*) port)->fileHandle, &fds);

		tv.tv_sec = timeout / 1000;
		tv.tv_usec = (timeout % 1000) * 1000;

      int selected = TEMP_FAILURE_RETRY (select(FD_SETSIZE, &fds, NULL, NULL, &tv));
		if (selected > 0)
		{
			int count = read(((Port*) port)->fileHandle, &bufp[len], maxlen - len);
			if (count < 0) 
			{
				__rcx_perror("read");
				return RCX_READ_FAIL;
			}
			len += count;
			timeout = 10; // reduce timout of following reads
		}
		else if (selected == 0)
		{
			break;
		}
		else if (selected < 0) 
		{
			__rcx_perror("select");
			return RCX_READ_FAIL;
		}
	}
	
	return len;
}

int __rcx_write(void* port, const void* buf, int len) 
{
	int written = TEMP_FAILURE_RETRY (write(((Port*) port)->fileHandle, buf, len));
	if (written < 0)
	{
		__rcx_perror("write");
		return RCX_WRITE_FAIL;
	}
}

void __rcx_purge(void* port)
{
	fd_set fds;
	FD_ZERO(&fds);
	FD_SET(((Port*) port)->fileHandle, &fds);

	struct timeval tv;
	tv.tv_sec = 0;
	tv.tv_usec = 0;

   int selected = TEMP_FAILURE_RETRY (select(FD_SETSIZE, &fds, NULL, NULL, &tv));
	if (selected > 0)
	{
      char bufp[BUFFERSIZE];
		int count = read(((Port*) port)->fileHandle, bufp, BUFFERSIZE);
		if (count < 0) 
		{
			__rcx_perror("read");
		}
	}
	else if (selected < 0) 
	{
		__rcx_perror("select");
	}
}

void __rcx_flush(void* port)
{
	// no working blocking flush for linux...
}

void* __rcx_open(char *tty, bool fast)
{
	if (__comm_debug) printf("tty = %s\n", tty);
	if (__comm_debug) printf("mode = %s\n", fast ? "fast" : "slow");

	bool success = true;

	Port* result = (Port*) malloc(sizeof(Port));
	__rcx_open_setDevice(result, tty, fast);
	
	result->fileHandle = open(result->deviceName, O_RDWR);
	if (result->fileHandle < 0) 
	{ 
		__rcx_perror("open");
		success = false;
	}
	else if (!result->usb)
	{
      // Setup serial port
      success = __rcx_open_setSerialPortParameters(result);
	}

	if (!success)
	{
		if (result->fileHandle < 0)
		{
			close(result->fileHandle);
		}
		free(result);
		return NULL;
	}
	
    if (__comm_debug) printf("device = %s\n", result->deviceName);
    if (__comm_debug) printf("port type = %s\n", result->usb? "usb" : "serial");

	return result;
}

bool __rcx_open_setSerialPortParameters (Port* port)
{
	struct termios ios;
	memset(&ios, 0, sizeof(ios));
    
	if (port->fast) 
	{
		ios.c_cflag = CREAD | CLOCAL | CS8;
		cfsetispeed(&ios, B4800);
		cfsetospeed(&ios, B4800);
	}
	else 
	{
		ios.c_cflag = CREAD | CLOCAL | CS8 | PARENB | PARODD;
		cfsetispeed(&ios, B2400);
		cfsetospeed(&ios, B2400);
	}
   
   int result = tcsetattr(((Port*) port)->fileHandle, TCSANOW, &ios);
   if (result < 0)
	{
		__rcx_perror("tcsetattr");
	}
   
	return result == -1;
}

void __rcx_close(void* port)
{
	int result = close(((Port*) port)->fileHandle);
	if (result < 0)
	{
		__rcx_perror("close");
	}
	free(port);
}
