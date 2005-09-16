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
 *  13/2/2005 Made interface generic C to avoid all the IOKit and Mach types impacting
 *  all the other code. Andy Belk
 *
 */

#ifndef osx_usb_h
#define osx_usb_h

#if defined (__cplusplus)
extern "C" {
#endif

#include <stdint.h>

/* Get a InterfaceInterface for the usb tower.
   Fast mode currently unsupported, and ignored.
   The first matching device is used for communication.
*/
intptr_t osx_usb_rcx_init (int is_fast);

/* Close an Interface opened by osx_usb_rcx_init */
void osx_usb_rcx_close (intptr_t intf);

int osx_usb_nbread (intptr_t intf, void *buf, int maxlen, int timeout);

int osx_usb_write(intptr_t intf, const void *msg, int msglen);

#if defined(__cplusplus)
}
#endif
#endif /* OSX_USB_H */


