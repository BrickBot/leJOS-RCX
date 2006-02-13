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
#	- Conditional make based on $OSTYPE variable for Windows NT (cygwin32)
# 2005.01.17 - Markus Heiden
#	- reworked, adapted to refactorings
#

PWD := $(shell pwd)
CC = $(shell $(LEJOS_HOME)/release/cctest.sh)

COPT = -O2
# For debugging:
# COPT = -O0

# Default: Unix
EXTRA_OS_SRCS = 
CFLAGS = -Wall -shared -fpic
LFLAGS = -shared
IRTRCX = $(BIN_TARGET)/libirtrcx.so
LINK = $(CC)

# Windows with CygWin installed
ifneq (,$(findstring cygwin,$(OSTYPE)))
	CFLAGS=-mno-cygwin -D__CYGWIN32__
	LFLAGS=-mno-cygwin -Wl,--add-stdcall-alias -shared
	IRTRCX=$(BIN_TARGET)/irtrcx.dll
endif

# MacOS X
ifneq (,$(findstring darwin,$(OSTYPE)))
	EXTRA_OS_SRCS = osx_usb.cpp
	CFLAGS = -g
	CFLAGS += $(shell $(LEJOS_HOME)/release/osx_universalbinary_flags.sh)
	LFLAGS = -dynamiclib -framework IOKit -framework CoreFoundation
	IRTRCX=$(BIN_TARGET)/libirtrcx.dylib
	override LINK=$(CXX) $(CFLAGS)
endif

SRCS = $(addprefix $(PWD)/, rcx_comm.c rcx_comm_$(OSTYPE).c $(EXTRA_OS_SRCS))
OBJS = $(addsuffix .o, $(basename $(SRCS)))

default: all

all: $(IRTRCX)

$(IRTRCX): $(OBJS)
	$(LINK) -o $(IRTRCX) $(OBJS) $(LFLAGS)

$(OBJS): $(SRCS) rcx_comm.h rcx_comm_os.h rcx_comm_$(OSTYPE).h

%.o: %.c
	$(CC) -c -o $@ $(COPT) $(CFLAGS) "$<"

%.o: %.cpp
	$(CXX) -c -o $@ $(COPT) $(CFLAGS) "$<"

.SUFFIXES: .cpp .o

clean:
	rm -f *.o
	rm -f $(IRTRCX)
