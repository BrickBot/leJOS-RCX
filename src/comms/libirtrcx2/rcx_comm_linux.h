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

/*
 * Programming note (aB.)
 * Shouldn't we use strcmp() which is a POSIX standard API (and works on Linux, Mac and any other Unix),
 * and special-case Windows ?
 */
#define stricmp(x, y) strcmp(x, y)
#define strnicmp(x, y, n) strncmp(x, y, n)

#endif

#define FILEDESCR	int

#endif /* RCX_COMM_LINUX_H_INCLUDED */
