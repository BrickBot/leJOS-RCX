# Makefile for libirtrcx : shared library for communicating with the RCX
# via serial and usb IR towers.
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
# Based on work by Kekoa Proudfoot.
# Portions created by Kekoa Proudfoot are Copyright (C) 1998, 1999
# Kekoa Proudfoot. All Rights Reserved.

#
# 2000.03.12 - Paolo Masetti <paolo.masetti@itlug.org>
#	- Conditional make based on $OSTYPE variable for Windows NT (cygnwin32)
#

#include ../Makefile.common

CC=$(shell $(LEJOS_HOME)/cctest.sh)

CFLAGS = -O2 

ifndef OSTYPE
	OSTYPE=$(MSDOS)
endif

ifneq (,$findstring $(OSTYPE), linux LINUX linux-gnu)
        CFLAGS=-O2 -I$(LEJOS_HOME)/src/tools/linux/legousbtower -shared -fpic	
	RCXCOMM_OS_C=rcx_comm_linux.c
	RCXCOMM_OS_H=rcx_comm_linux.h
	RCXCOMM_OS_OBJ=rcx_comm_linux.o
	LIBIRTRCX=$(LEJOS_HOME)/bin/libirtrcx.so
endif

ifneq (,$(findstring $(OSTYPE),cygwin32 cygwin_nt-5.0 cygwin_nt-5.1 CYGWIN_NT-4.0 WindowsNT Windows_NT))
	EXT=.exe
	LIBIRTRCX=$(LEJOS_HOME)/bin/irtrcx.dll
        CFLAGS=-mno-cygwin -Wl,--add-stdcall-alias -shared -D__CYGWIN32__
	RCXCOMM_OS_C=rcx_comm_win.c
	RCXCOMM_OS_H=rcx_comm_win.h
	RCXCOMM_OS_OBJ=rcx_comm_win.o
endif
ifneq (,$(findstring darwin,$(OSTYPE)))
	RCXCOMM_OS_C=osx_usb.c
	RCXCOMM_OS_H=osx_usb.h
	RCXCOMM_OS=osx_usb
	LIBS += -framework IOKit -framework CoreFoundation
        CFLAGS += -g
else
	OSX_USB=
endif

all: *.o 

.SUFFIXES: .c .o

*.o:
	$(CC) $(RCXCOMM_OS_C) rcx_comm.c $(CFLAGS) -o $(LIBIRTRCX)

clean:
	rm -f *.o *~ *.bak *.a
