/*
 *  rcx_comm_linux.h
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

#ifndef RCX_COMM_LINUX_H_INCLUDED
#define RCX_COMM_LINUX_H_INCLUDED

#if defined(LINUX) || defined(linux)
#define TOWER_NAME "/dev/usb/legousbtower0"
#define DEFAULT_PORT   "usb"       /* Linux - USB */

#elif defined (sun)
#define DEFAULT_PORT   "/dev/ttya"  /* Solaris - first serial port - untested */

#else
#define DEFAULT_PORT   "/dev/ttyd2" /* IRIX - second serial port */
#endif

#define FILEDESCR	int
#define BADFILE	-1

#endif /* RCX_COMM_LINUX_H_INCLUDED */

