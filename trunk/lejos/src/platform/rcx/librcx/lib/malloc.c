/*
 *  malloc.c
 *
 *  A simple, compact implementation of malloc for the H8/300.  Maintains a
 *  singly-linked list of blocks.  Malloc starts at head and searches the
 *  list for a sufficiently large free block, gathering free blocks along
 *  the way.  Free marks block as free and does no additional work.
 *
 *  Beware, there are no magic numbers to keep you from corrupting memory.
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
 *  The Original Code is Librcx code, released February 9, 1999.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  Contributor(s): Kekoa Proudfoot <kekoa@graphics.stanford.edu>
 */

/* stdlib.h is a hack, for now */
#include "stdlib.h"

/* Blocks are sorted and are chained together by their lengths only */
/* Sign bit of length is used to indicate allocated or not */
/* Note that the size of free memory is always less than 0x8000 */

typedef struct {
    size_t len;
} mem_hdr;

extern char _end;

#define ALIGN(s)   (((s) + 1) & 0xfffe)
#define FIRST      ((char *)&_end)
#define LAST       ((char *)0xef00)   /* should really get this from linker */
#define HDR(p)     ((mem_hdr *)(p))
#define LEN(p)     (HDR(p)->len)      /* equals true length only if free */
#define ALEN(p)    (LEN(p) & 0x7fff)  /* always equals true length */
#define ALLOC(p)   (LEN(p) |= 0x8000)
#define FREE(p)    (LEN(p) &= 0x7fff)
#define ISFREE(p)  ((int)LEN(p) >= 0)
#define HTOD(p)    (&HDR(p)[1])
#define DTOH(p)    (&HDR(p)[-1])
#define HSIZE      (ALIGN(sizeof(mem_hdr)))
#define MIN        (HSIZE + ALIGN(4)) /* must be at least HSIZE */



void
malloc_init (void)
{
    /* Hack? Maybe check that FIRST + HSIZE <= LAST */
    LEN(FIRST) = LAST - FIRST;
}

/* Hack! */
#include "rom.h"

void *
malloc (size_t size)
{
    char *ptr, *next;
    size_t alloc;

    /* Align to word boundary */
    alloc = HSIZE + ALIGN(size);

    /* Check for overflow */
    if (alloc >= size) {
	/* Find the first free block that has enough space */
	for (ptr = next = FIRST; ptr < LAST; ptr = next) {
	    if (ISFREE(ptr)) {
		/* Join free blocks */
		next += LEN(ptr);
		while (next < LAST && ISFREE(next)) {
		    LEN(ptr) += LEN(next);
		    next += LEN(next);
		}
		/* Check if large enough */
		if (LEN(ptr) >= alloc) {
		    /* Split if remainder is at least size MIN */
		    if (LEN(ptr) >= alloc + MIN) {
			/* Hack? Change to allocate later section */
			LEN(ptr+alloc) = LEN(ptr) - alloc;
			LEN(ptr) = alloc;
		    }
		    /* Mark as allocated */
		    ALLOC(ptr);
		    return HTOD(ptr);
		}
	    }
	    else
		/* Advance to next, assuming not free */
		next += ALEN(ptr);
	}
    }
    return NULL;
}

void
free (void *ptr)
{
    /* Mark as free */
    FREE(DTOH(ptr));
}
