/**
 * lejosrun.c
 * By Paul Andrews based heavily on firmdl.c
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <termios.h>
#include <stdio.h>
#include <string.h>
#include <sys/time.h>
#include <ctype.h>
#include <string.h>

#if defined(_WIN32) || defined(__CYGWIN32__)
  #include <windows.h>
#endif

#include <magic.h>

#include "firmdl/rcx_comm.h"

#ifdef __APPLE__
#include <CoreFoundation/CoreFoundation.h>

#include <IOKit/IOKitLib.h>
#include <IOKit/IOCFPlugIn.h>
#include <IOKit/usb/IOUSBLib.h>
#include <IOKit/usb/USBSpec.h>

#include "firmdl/osx_usb.h"
#endif /* __APPLE__ */

typedef unsigned char byte;

// This is for CygWin:

#ifndef O_BINARY
#define O_BINARY 0
#endif

#if defined(LINUX) || defined(linux)
#define DEFAULTTTY   "/dev/ttyS0" /* Linux - COM1 */
#elif defined (_WIN32) || defined(__CYGWIN32__)
#define DEFAULTTTY   "com1"       /* Cygwin - COM1 */
#elif defined (sun)
#define DEFAULTTTY   "/dev/ttya"  /* Solaris - first serial port - untested */
#elif defined (__APPLE__)
#define DEFAULTTTY   "usb"	  /* Default to USB on MAC */
#else
#define DEFAULTTTY   "/dev/ttyd2" /* IRIX - second serial port */
#endif

extern int __comm_debug;
char *progname;
int usb_flag = 0;

/* RCX routines */

#define BUFFERSIZE      4096
#define TOWRITEMAX      100
#define RETRIES         5
#define WAKEUP_TIMEOUT  4000
#define RESPONSE_LENGTH 11

long transfer_data (FILEDESCR fd, byte opcode, ushort index, byte *buffer, long length)
{
  byte *actualBuffer;
  byte checkSum;
  byte response[RESPONSE_LENGTH];
  int retries = RETRIES;
  long  actualLength;
  long  r = 0, i;

  actualLength = length + 6;
  actualBuffer = (byte *) malloc (actualLength);
  actualBuffer[0] = opcode;
  actualBuffer[1] = (byte) ((index >> 0) & 0xFF);
  actualBuffer[2] = (byte) ((index >> 8) & 0xFF);
  actualBuffer[3] = (byte) ((length >> 0) & 0xFF);
  actualBuffer[4] = (byte) ((length >> 8) & 0xFF);
  checkSum = 0;
  // Don't include opcode in this checksum!
  for (i = 0; i < length; i++)
  {
    checkSum += buffer[i];
    actualBuffer[5 + i] = buffer[i];
  }
  actualBuffer[5 + i] = checkSum;
  r = rcx_sendrecv(
          fd,             // FD
          actualBuffer,   // send buffer
          actualLength,   // send length
          response,       // receive buffer
          2,              // receive length
          length == TOWRITEMAX ? 100 : 200,             // receive timeout ms
          RETRIES,        // num tries
          1               // use complements
      );
  if (r >=0 && response[1] == 3)
  {
      printf("Checksum failed\n");
      r = -3;
  }
  free (actualBuffer);
  return r;
}

#ifdef __APPLE__
long osx_usb_transfer_data (IOUSBInterfaceInterface** intf, byte opcode, ushort index, byte *buffer, long length)
{
    byte *actualBuffer;
    byte checkSum;
    byte response[RESPONSE_LENGTH];
    int retries = RETRIES;
    long  actualLength;
    long  r = 0, i;

    actualLength = length + 6;
    actualBuffer = (byte *) malloc (actualLength);
    actualBuffer[0] = opcode;
    actualBuffer[1] = (byte) ((index >> 0) & 0xFF);
    actualBuffer[2] = (byte) ((index >> 8) & 0xFF);
    actualBuffer[3] = (byte) ((length >> 0) & 0xFF);
    actualBuffer[4] = (byte) ((length >> 8) & 0xFF);
    checkSum = 0;
    // Don't include opcode in this checksum!
    for (i = 0; i < length; i++)
    {
        checkSum += buffer[i];
        actualBuffer[5 + i] = buffer[i];
    }
    actualBuffer[5 + i] = checkSum;
    r = osx_usb_rcx_sendrecv(
                             intf,             // USBInterfaceInterface
                             actualBuffer,   // send buffer
                             actualLength,   // send length
                             response,       // receive buffer
                             2,              // receive length
                             length == TOWRITEMAX ? 100 : 200,             // receive timeout ms
                             RETRIES,        // num tries
                             1               // use complements
                             );
    if (r >=0 && response[1] == 3)
    {
        printf("Checksum failed\n");
        r = -3;
    }
    free (actualBuffer);
    return r;
}
#endif

int
main(int argc, char **argv)
{
    byte *pBinary;
    byte *pSend;
    byte response[RESPONSE_LENGTH];
    byte send[3];
    byte opcode;
    char *fileName = 0;
    long i, status;
    long numRead;
    int use_fast= 0;
    int usage = 0;
    char *tty = 0;
#ifdef __APPLE__
    IOUSBInterfaceInterface **intf = NULL;
#endif
    FILEDESCR fd = 0;
    long pDesc, pLength, pTotal;
    long r, index, rest, numToWrite, offset;

    /* Parse command line */

    progname = argv[0];
    
    argv++; argc--;
    while (argc && argv[0][0] == '-') {
	if (argv[0][1] == '-') {
	    if (!strcmp(argv[0], "--")) {
		argv++; argc--;
		break;
	    }
	    else if (!strcmp(argv[0], "--debug")) {
		__comm_debug = 1;
	    }
	    else if (!strcmp(argv[0], "--fast")) {
		use_fast = 1;
	    }
	    else if (!strcmp(argv[0], "--slow")) {
		use_fast = 0;
	    }
	    else if (!strncmp(argv[0], "--tty", 5)) {
		if (argv[0][5] == '=') {
		    tty = &argv[0][6];
		}
		else if (argc > 1) {
		    argv++; argc--;
		    tty = argv[0];
		}
		else
		    tty = "";
		if (!tty[0]) {
		    fprintf(stderr, "%s: invalid tty: %s\n", progname, tty);
		    exit(1);
		}
	    }
	    else if (!strcmp(argv[0], "--help")) {
		usage = 1;
	    }
	    else {
		fprintf(stderr, "%s: unrecognized option %s\n",
			progname, argv[0]);
		exit(1);
	    }
	}
	else {
	    char *p = &argv[0][1];
	    if (!*p)
		break;
	    while (*p) {
		switch (*p) {
		case 'f': use_fast = 1; break;
		case 's': use_fast = 0; break;
		case 'h': usage = 1; break;
		default:
		    fprintf(stderr, "%s: unrecognized option -- %c\n",
			    progname, *p);
		    exit(1);
		}
		p++;
	    }
	}
	argv++;
	argc--;
    }

    if (argc == 1) {
      fileName = argv[0];
    } else {
      usage = 1;
    }
    
    if (usage) {
	char *usage_string =
	    "      --debug      show debug output, mostly raw bytes\n"
	    "  -f, --fast       use fast 4x downloading\n"
	    "  -s, --slow       use slow 1x downloading (default)\n"
	    "      --tty=TTY    assume tower connected to TTY\n"
	    "      --tty=usb    assume tower connected to usb\n"
	    "  -h, --help       display this help and exit\n"
	    ;

	fprintf(stderr, "usage: %s [options] filename\n", progname);
	fprintf(stderr, usage_string);
	exit(1);
    }

    if ((pDesc = open(fileName, O_RDONLY | O_BINARY)) == -1) {
	fprintf(stderr, "%s: failed to open file %s\n", argv[0], argv[1]);
	exit(1);
    }

    /* Open the serial port */

    if (tty == NULL)
    {
        tty = getenv("RCXTTY");
        if (tty == NULL)
        {
          printf ("RCXTTY not defined. Using: %s\n", DEFAULTTTY);
          tty = DEFAULTTTY;
        }
    }

#if defined (_WIN32) || defined(__CYGWIN32__)
    /* stricmp not available on Linux */
    if ( stricmp( tty , "usb" ) == 0 ) {
	usb_flag = 1;
	if ( __comm_debug ) fprintf(stderr, "USB IR Tower mode.\n");
	tty="\\\\.\\legotower1";
    }
#endif

    /* FIXME: Check for tty or usb here */
#ifdef __APPLE__
    if ( strcmp( tty , "usb" ) == 0 ) {
        usb_flag = 1;
        if ( __comm_debug) fprintf(stderr, "USB IR Tower mode.\n");
    }
#endif
    /* Wake up the tower */
#ifdef __APPLE__
    if (usb_flag == 0)
    {
#endif
        fd = rcx_init(tty, 0);
#ifdef __APPLE__
    }
    else
    {
        intf = osx_usb_rcx_init(0);
    }
#endif
    
    if (usb_flag == 0)
    {
        // usb do not need wakeup tower.
	if ((status = rcx_wakeup_tower(fd, WAKEUP_TIMEOUT)) < 0) {
	    fprintf(stderr, "%s: %s\n", progname, rcx_strerror(status));
	    exit(1);
	}
    }

#ifdef __APPLE__
    if (usb_flag == 0) {
#endif
        
	if (!rcx_is_alive(fd, 1)) {
	    /* See if alive in fast mode */

	    rcx_close(fd);
	    fd = rcx_init(tty, 1);

	    if (rcx_is_alive(fd, 0)) {
		fprintf(stderr, "%s: rcx is in fast mode\n", progname);
		fprintf(stderr, "%s: turn rcx off then back on to "
			"use slow mode\n", progname);
		exit(1);
	    }

	    fprintf(stderr, "%s: no response from rcx\n", progname);
	    exit(1);
	}
#ifdef __APPLE__
    }
    else
    {
        if (!osx_usb_rcx_is_alive(intf, 1))
        {
            osx_usb_rcx_close(intf);
            fprintf(stderr, "%s: no response from rcx\n", progname);
            exit(1);
        }
    }
#endif
    
    // Read in file
    pLength = lseek (pDesc, 0, SEEK_END);
    if (pLength > 0xFFFF)
    {
      printf ("Huge file: %d bytes\n", (int) pLength);
      exit (1);
    }
    lseek (pDesc, 0, SEEK_SET);
    pBinary = (void *) malloc (pLength);
    pTotal = 0;
    while (pTotal < pLength)
    {
      r = read (pDesc, pBinary + pTotal, pLength - pTotal);
      if (r == -1 || r == 0)
      {
        printf ("Unexpected EOF in %s. Read only %ld bytes.\n", argv[1], pTotal);
        exit (1);
      }
      pTotal += r;
    }
    if (pBinary[0] != ((MAGIC >> 8) & 0xFF) ||
        pBinary[1] != ((MAGIC >> 0) & 0xFF))
    {
      printf ("Magic number is not right. Linker used was for emulation only?\n");
      exit (1);
    }

    // Send program-download message
    send[0] = 0x12;
    send[1] = (byte) (MAGIC >> 8);
    send[2] = (byte) (MAGIC & 0xFF);
#ifdef __APPLE__
    if (usb_flag == 0)
    {
#endif        
    numRead = rcx_sendrecv(
        fd,             // FD
        send,           // send buffer
        3,              // send length
        response,       // receive buffer
        3,              // receive length
        100,             // timeout ms
        1,              // num tries
        1               // use complements
    );
#ifdef __APPLE__
    }
    else
    {
        numRead = osx_usb_rcx_sendrecv(
                                       intf,             // FD
                                       send,           // send buffer
                                       3,              // send length
                                       response,       // receive buffer
                                       3,              // receive length
                                       100,             // timeout ms
                                       1,              // num tries
                                       1               // use complements
                                       );
    }
#endif
    if (numRead != 3)
    {
      printf (numRead == -1 ? "No response from RCX. " : "Bad response from RCX. ");
	  printf ("Status = %s. ", rcx_strerror(numRead));
      printf ("Please make sure RCX has leJOS firmware "
              "and is in range. The firmware must be in program download mode. "
	      "Turn RCX off and on if necessary.\n");
      exit (1);
    }
        
    if (response[1] != send[1] || response[2] != send[2])
    {
      printf ("Unexpected response from RCX. The RCX either doesn't have valid leJOS firmware or "
              "it is not in program-download mode. (lejosfirmdl downloads firmware).\n");
      exit (1);
    }

    // Transfer data    
    index = 0;
    rest = pLength;
    offset = 0;
    opcode = 0x45;
    do {
      numToWrite = (rest > TOWRITEMAX) ? TOWRITEMAX : rest;
      index = (rest > TOWRITEMAX) ? index + 1 : 0;
      fprintf(stderr, "\r%3ld%%        \r",(100*offset)/pLength);
#ifdef __APPLE__
      if (usb_flag == 0)
      {
#endif
          if ((status = transfer_data (fd, opcode, index, pBinary + offset, numToWrite)) < 0)
      {
          printf("Unexpected response from RCX whilst downloading: %ld\n", status);
          exit(1);
      }
#ifdef __APPLE__
      }
      else
      {
          if ((status = osx_usb_transfer_data (intf, opcode, index, pBinary + offset, numToWrite)) < 0)
          {
              printf("Unexpected response from RCX whilst downloading: %ld\n", status);
              osx_usb_rcx_close(intf);
              exit(1);
          }
      }
#endif /* __APPLE__ */
      opcode ^= 0x08;
      rest -= numToWrite;
      offset += numToWrite;
    } while (index != 0);
#ifdef __APPLE__
    if (usb_flag == 0)
    {
#endif
        rcx_close(fd);
#ifdef __APPLE__
    }
    else
    {
        osx_usb_rcx_close(intf);
    }
#endif
    fprintf(stderr, "\r100%%        \r");
    exit(0);
}
