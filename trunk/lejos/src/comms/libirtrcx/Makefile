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
# For debugging:
#CFLAGS = -O0

ifndef OSTYPE
	OSTYPE=$(MSDOS)
endif

ifneq (,$(findstring $(OSTYPE), linux LINUX linux-gnu))
	RCXCOMM_OS_C=rcx_comm_linux.cpp
	RCXCOMM_OS_H=rcx_comm_linux.h
	CFLAGS+=-I$(LEJOS_HOME)/src/tools/linux/legousbtower -shared -fpic	
	LIBIRTRCX=$(LEJOS_HOME)/bin/libirtrcx.so
	LFLAGS=-shared
endif

ifneq (,$(findstring $(OSTYPE),cygwin32 cygwin_nt-5.0 cygwin_nt-5.1 CYGWIN_NT-4.0 WindowsNT Windows_NT))
	RCXCOMM_OS_C=rcx_comm_win.cpp
	RCXCOMM_OS_H=rcx_comm_win.h
	EXT=.exe
	LIBIRTRCX=$(LEJOS_HOME)/bin/irtrcx.dll
	CFLAGS=-mno-cygwin -D__CYGWIN32__
	LFLAGS=-mno-cygwin -Wl,--add-stdcall-alias -shared
endif

ifneq (,$(findstring $(OSTYPE), darwin))
	RCXCOMM_OS_C=rcx_comm_osx.cpp osx_usb.cpp
	RCXCOMM_OS_H=rcx_comm_osx.h osx_usb.h
	CFLAGS += -g
	LIBS += -framework IOKit -framework CoreFoundation
	LFLAGS = -dynamiclib
	LIBIRTRCX=$(LEJOS_HOME)/bin/libirtrcx.dylib
endif

SRCS = $(RCXCOMM_OS_C) rcx_comm.cpp
OBJS = $(addsuffix .o, $(basename $(SRCS)))

all: $(LIBIRTRCX)
	@echo Built $(LIBIRTRCX)

$(LIBIRTRCX): $(OBJS)
	$(CC) -o $(LIBIRTRCX) $(OBJS) $(LFLAGS) $(LIBS)

$(OBJS): $(SRCS) $(RCXCOMM_OS_H)

%.o: %.cpp
	$(CC) -c -o $@ $(CFLAGS) "$<"

.SUFFIXES: .cpp .o

clean:
	rm -f *.o *~ *.bak *.a $(LIBIRTRCX)
