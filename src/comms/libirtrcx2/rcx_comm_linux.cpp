/*
 *  rcx_comm_linux.c
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

#define USB_TOWER_NAME "/dev/usb/lego0"

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

	FILEDESCR fd = ((Port*) port)->fileHandle;
	fd_set fds;

	struct timeval tv;
	tv.tv_sec = timeout / 1000;
	tv.tv_usec = (timeout % 1000) * 1000;

	long len = 0;
	int retry = 10;

	while (len < maxlen) 
	{
		FD_ZERO(&fds);
		FD_SET(fd, &fds);

      int selected = select(1, &fds, NULL, NULL, &tv);
		if (selected > 0)
		{
			int count = read(fd, &bufp[len], maxlen - len);
			if (count < 0) 
			{
				perror("read");
				return RCX_READ_FAIL;
			}
			len += count;
		}
		else if (selected == 0 && (len > 0 || retry-- == 0))
		{
			break;
		}
		else if (selected < 0) 
		{
			perror("select");
			return RCX_READ_FAIL;
		}
	}
	
	return len;
}

int __rcx_write(void* port, void* buf, int len) 
{
	return write(((Port*) port)->fileHandle, buf, len);
}

void __rcx_purge(void* port)
{
	char echo[BUFFERSIZE];
	__rcx_read(port, echo, BUFFERSIZE, 1);
}

void __rcx_flush(void* port)
{
	fsync(((Port*) port)->fileHandle);
}

void* __rcx_open(char *tty, bool fast)
{
	if (__comm_debug) printf("mode = %s\n", fast ? "fast" : "slow");
	if (__comm_debug) printf("tty = %s\n", tty);

	bool success = true;

	Port* result = (Port*) malloc(sizeof(Port));
	__rcx_open_setDevice(result, tty, fast);
	
   if (__comm_debug) printf("device = %s\n", result->deviceName);
	if (__comm_debug) printf("port type = %s\n", result->usb? "usb" : "serial");

	result->fileHandle = open(result->deviceName, O_RDWR);
	if (result->fileHandle < 0) 
	{ 
		if (__comm_debug) printf("Error %lu: Opening %s\n", errno, result->deviceName);
		success = false;
	}
	else if (!result->usb)
	{
      // Setup serial port
      success = __rcx_open_setSerialPortParameters(result);
		if (__comm_debug && !success) printf("Error %lu: Setting serial port parameters for %s\n", errno, result->deviceName);
	}

	if (!success)
	{
		if (result->fileHandle >= 0)
		{
			close(result->fileHandle);
		}
		free(result);
		return NULL;
	}
	
	return result;
}

void __rcx_open_setDevice (Port* port, char* symbolicName, bool fast)
{
	strncpy(port->symbolicName, symbolicName, 32);
   port->symbolicName[31] = 0;
   
   int length = strlen(symbolicName);
	if (strncmp(symbolicName, "usb", 3) == 0 && length <= 4)
	{
		// usb mode (does _not_ support doubled baud rate)
      strncpy(port->deviceName, USB_TOWER_NAME, 32);
      if (length == 4)
      {
      	// multiple usb tower mode
      	port->deviceName[strlen(USB_TOWER_NAME) - 1] = symbolicName[3];
      }
      port->usb = true;	
      port->fast = fast; // 2x: no complements
	}
	else
	{
		// serial mode
		strncpy(port->deviceName, symbolicName, 32);
		port->deviceName[31] = 0;
		port->usb = false;	
      port->fast = fast; // 4x: no complements, doubled baud rate
	}
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
    
	return tcsetattr(port->fileHandle, TCSANOW, &ios) != -1;
}

void __rcx_close(void* port)
{
	close(((Port*) port)->fileHandle);
	free(port);
}
