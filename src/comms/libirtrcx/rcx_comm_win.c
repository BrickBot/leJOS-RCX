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
#include <windows.h>

typedef struct timeval timeval_t;
void gettimeofday(struct timeval *tv, void *tzp);

#include "rcx_comm.h"
#include "rcx_comm_win.h"

/* Defines */

#define BUFFERSIZE  4096

/* Globals */

extern int __comm_debug;
extern int usb_flag;





void __rcx_perror(char *str) 
{
	if (__comm_debug)fprintf(stderr, "Error %lu: %s\n", (unsigned long) GetLastError(), str);
}

/* Timeout read routine */

int __rcx_read (FILEDESCR fd, void *buf, int maxlen, int timeout)
{
	char *bufp = (char *)buf;
	int len = 0;

	while (len < maxlen) {

		if ( usb_flag == 1) {
			DWORD count = 0;
			struct timeval timebegin ,now;
			unsigned long elapsed;

			gettimeofday(&timebegin,0);
		
			while ( count == 0 ) {
				ReadFile( fd, &bufp[len], maxlen - len, &count, NULL);
				gettimeofday(&now,0);
				elapsed = (now.tv_sec - timebegin.tv_sec ) + now.tv_usec - timebegin.tv_usec;				
				if  ( elapsed > timeout )
					break;
			}
			if (count == 0) {
				if ( __comm_debug  )
					printf("usb mode: nbread .. time out break\n");
				break;
			}
			len += count;

		} else {
			DWORD count = 0;
			COMMTIMEOUTS CommTimeouts;
        
			GetCommTimeouts (fd, &CommTimeouts);

			// Change the COMMTIMEOUTS structure settings.
			CommTimeouts.ReadIntervalTimeout = MAXDWORD;
			CommTimeouts.ReadTotalTimeoutMultiplier = 0;
			CommTimeouts.ReadTotalTimeoutConstant = timeout;
			CommTimeouts.WriteTotalTimeoutMultiplier = 10;
			CommTimeouts.WriteTotalTimeoutConstant = 1000;

			// Set the time-out parameters for all read and write operations
			// on the port.
			SetCommTimeouts(fd, &CommTimeouts);

			if (ReadFile(fd, &bufp[len], maxlen - len, &count, NULL) == FALSE) {
				__rcx_perror("ReadFile");
				fprintf(stderr, "nb_read - error reading tty: %lu\n", (unsigned long) GetLastError());
				return RCX_READ_FAIL;
			}

			len += count;

			if (count == 0) {
				//timeout
				break;
			}
		}
	}

	return len;
}

/* discard all characters in the input queue of tty */
void __rcx_flush(FILEDESCR fd)
{
	PurgeComm(fd, PURGE_RXABORT | PURGE_RXCLEAR);
}

int __rcx_write(FILEDESCR fd, const void *buf, size_t len) 
{
	DWORD nBytesWritten=0;
	if (!WriteFile(fd, buf, len, &nBytesWritten, NULL))
		return RCX_WRITE_FAIL;
	FlushFileBuffers(fd);
	return nBytesWritten;
}

/* RCX routines */

FILEDESCR __rcx_init(char *tty, int is_fast)
{
	FILEDESCR fd;

	DCB dcb;

	if (__comm_debug) printf("mode = %s\n", is_fast ? "fast" : "slow");
	if (__comm_debug) printf("tty= %s\n", tty);

	if (__comm_debug) printf("Running under cygwin\n");
	if ((fd = CreateFile(tty, GENERIC_READ | GENERIC_WRITE,
			     0, NULL, OPEN_EXISTING,
			     0, NULL)) == INVALID_HANDLE_VALUE) {
		fprintf(stderr, "Error %lu: Opening %s\n", (unsigned long) GetLastError(), tty);
		return NULL;
	}

	// Serial settings
	if (usb_flag == 0)
		{
			FillMemory(&dcb, sizeof(dcb), 0);
			if (!GetCommState(fd, &dcb)) {	// get current DCB
				// Error in GetCommState
				__rcx_perror("GetCommState");
				return NULL;
			} else {
				dcb.ByteSize = 8;
				dcb.Parity   = (is_fast ? 0 : 1);	// 0-4=no,odd,even,mark,space
				dcb.StopBits = 0;			// 0,1,2 = 1, 1.5, 2
				dcb.fBinary  = TRUE ;
				dcb.fParity  = (is_fast ? FALSE : TRUE) ;
				dcb.fAbortOnError = FALSE ;
				dcb.BaudRate = (is_fast ? CBR_4800 : CBR_2400);	// Update DCB rate.

				// Set new state.
				if (!SetCommState(fd, &dcb)) {
					// Error in SetCommState. Possibly a problem with the communications
					// port handle or a problem with the DCB structure itself.
					__rcx_perror("SetCommState");
					return NULL;
				}
			}
		}

	return fd;
}

void __rcx_close(FILEDESCR fd)
{
	CloseHandle(fd);
}


void gettimeofday(timeval_t *tv, void *tzp) {
	SYSTEMTIME st;
	GetSystemTime(&st);
	tv->tv_sec = (st.wHour) * 3600 + (st.wMinute) * 60 + st.wSecond;
	tv->tv_usec = st.wMilliseconds * 1024;
}


