/*
 *  rcx_comm_win.h
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
 */

#ifndef RCX_COMM_WIN_H_INCLUDED
#define RCX_COMM_WIN_H_INCLUDED

#include <windows.h>

#define USB_TOWER_NAME "\\\\.\\LEGOTOWER1"
#define DEFAULTTTY "usb"

#define FILEDESCR HANDLE

extern void* __rcx_open (char *tty, int fast);
extern void __rcx_close (void* port);
extern int __rcx_write(void* port, void* buffer, int length);
extern int __rcx_read(void* port, void* buffer, int maxLength, int timeout_ms);
extern void __rcx_flush(void* port);

#define usleep(x) Sleep(x/1000)

#endif /* RCX_COMM_WIN_H_INCLUDED */

