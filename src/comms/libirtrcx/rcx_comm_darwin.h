/*
 *  rcx_comm_osx.h
 *
 *  Platform specific linux RCX communication routines.
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

#ifndef RCX_COMM_OSX_H_INCLUDED
#define RCX_COMM_OSX_H_INCLUDED

#define TOWER_NAME ""
#define DEFAULTTTY   "usb"	  /* Default to USB on MAC */
#define stricmp(x, y) strcmp(x, y)
#define strnicmp(x, y, n) strncmp(x, y, n)

/* Support storage of file descriptors or IODevices in a FILEDESCR */
#include <stdint.h>
#define FILEDESCR	intptr_t
#define BADFILE	-1
/* O_SYNC not defined on earlier Mac OS X versions */
#ifndef O_SYNC
#define O_SYNC 0x0080
#endif

#endif /* RCX_COMM_OSX_H_INCLUDED */
