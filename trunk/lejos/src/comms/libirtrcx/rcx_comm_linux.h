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

#define FILEDESCR	int
#define BADFILE	-1


extern int       __rcx_write(FILEDESCR fd, const void *buf, size_t len);
extern int       __rcx_read(FILEDESCR fd, void *buf, int maxlen, int timeout);
extern FILEDESCR __rcx_init (char *tty, int is_fast);
extern void      __rcx_close (FILEDESCR fd);
extern int __rcx_send(FILEDESCR fd, void *buf, int len, int use_comp);
extern int __rcx_recv(FILEDESCR fd, void *buf, int maxlen, int timeout, int use_comp);
extern int __rcx_is_alive (FILEDESCR fd, int use_comp);
extern int __rcx_sendrecv (FILEDESCR fd, void *send, int slen, void *recv, int rlen, int timeout, int retries, int use_comp);

#endif /* RCX_COMM_LINUX_H_INCLUDED */

