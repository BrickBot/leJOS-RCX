# shared Makefile

# The contents of this file are subject to the Mozilla Public License
# Version 1.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
# http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS"
# basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
# License for the specific language governing rights and limitations
# under the License.
#
# The Original Code is Librcx code, released February 9, 1999.
#
# The Initial Developer of the Original Code is Kekoa Proudfoot.
# Portions created by Kekoa Proudfoot are Copyright (C) 1999
# Kekoa Proudfoot. All Rights Reserved.

# programs

CC = $(BINDIR)/$(BINPREFIX)gcc
AS = $(BINDIR)/$(BINPREFIX)as
AR = $(BINDIR)/$(BINPREFIX)ar
LD = $(BINDIR)/$(BINPREFIX)ld -T$(ROOT)/lib/rcx.lds -u__start

# other variables

COFF = --oformat coff-h8300

# special targets

.PHONY: all clean
.PRECIOUS: %.o

# pattern rules

%.o: %.c
	$(CC) $(CFLAGS) $(INCS) -c $*.c
%.o: %.s
	$(AS) $*.s -o $*.o
%.s: %.c
	$(CC) $(CFLAGS) $(INCS) -S $*.c
%.a:
	rm -f $*.a
	$(AR) rc $*.a $(filter %.o,$^)
%.srec: %.o
	$(LD) $(LFLAGS) -o $*.srec $(filter %.o,$^) $(LIBS)
%.coff: %.o
	$(LD) $(LFLAGS) $(COFF) -o $*.coff $(filter %.o,$^) $(LIBS)

# includes

-include $(ROOT)/Config
