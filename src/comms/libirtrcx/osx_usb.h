/*
 *  $Id$
 *  RCX communication routines for USB on OS X.
 *  By Markus Strickler <markus@braindump.ms> 
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
 *
 *  This code builds heavily on the Firmdl Code, released October 3, 1998.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1998, 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  OS X specific code taken in parts from Dave Baum's OS X implementation of NCQ.
 *
 */
#ifndef osx_usb_h
#define osx_usb_h

#include <CoreFoundation/CoreFoundation.h>
#include <unistd.h>
#include <sys/time.h>

#include <IOKit/IOKitLib.h>
#include <IOKit/IOCFPlugIn.h>
#include <IOKit/usb/IOUSBLib.h>
#include <IOKit/usb/USBSpec.h>

#define LegoUSBRelease 256

#define LEGO_SEND_PIPE 2
#define LEGO_RECV_PIPE 1

#define OSX_USB_BUFFERSIZE 4096
#ifndef RCX_COMM_H_INCLUDED
#include "rcx_comm.h"
#endif

/* #ifndef OSX_DEBUG
#define OSX_DEBUG 1
#endif */

static io_iterator_t		gRawAddedIter;

/* Get a InterfaceInterface for the usb tower.
   Fast mode currently unsupported, and ignored.
   The first matching device is used for communication.
*/
IOUSBInterfaceInterface** osx_usb_rcx_init (int is_fast);

/* Close an Interface opened by osx_usb_rcx_init */
void osx_usb_rcx_close (IOUSBInterfaceInterface** intf);

int osx_usb_rcx_is_alive (IOUSBInterfaceInterface **intf, int use_comp);

int osx_usb_rcx_sendrecv (IOUSBInterfaceInterface **intf, void *send, int slen, void *recv, int rlen, int timeout, int retries, int use_comp);

int osx_usb_rcx_recv (IOUSBInterfaceInterface **intf, void *buf, int maxlen, int timeout, int use_comp);

int osx_usb_rcx_send(IOUSBInterfaceInterface **intf, void *buf, int len, int use_comp);

#endif /* OSX_USB_H */


