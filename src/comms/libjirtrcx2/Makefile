#
# Makefile for jni adapter for librcx
#

PWD = $(shell pwd)
SRCS = $(addprefix $(PWD)/, Tower.cpp)

CC=$(shell $(LEJOS_HOME)/release/cctest.sh)

JAVA_INCL="$(JAVA_HOME)"/include

IRTRCX_INCL="$(LEJOS_HOME)/src/comms/libirtrcx2"
IRTRCX_H="$(IRTRCX_INCL)/rcx_comm.h"

# Default: Unix
JIRTRCX=$(BIN_TARGET)/libjirtrcx.so
CCOPT=-rdynamic -ldl -shared -fpic -I${JAVA_INCL} -I${JAVA_INCL}/$(OSTYPE) -I$(IRTRCX_INCL) -L$(BIN_TARGET) -lirtrcx

# Windows with CygWin installed
ifneq (,$(findstring cygwin,$(OSTYPE)))
  JIRTRCX=$(BIN_TARGET)/jirtrcx.dll
  CCOPT= -mno-cygwin -D__CYGWIN32__ -I$(JAVA_INCL) -I$(JAVA_INCL)/win32 -I$(IRTRCX_INCL) -Wl,--add-stdcall-alias -shared -L"$(BIN_TARGET)" -lirtrcx
endif

# MacOS X
ifneq (,$(findstring darwin,$(OSTYPE)))
  JIRTRCX=$(BIN_TARGET)/libjirtrcx.jnilib 
  # Mac OS X compiles PIC by default
  CFLAGS = -g -O -I$(IRTRCX_INCL) -I/System/Library/Frameworks/JavaVM.framework/Headers -I$(IRTRCX_INCL) 
  LFLAGS = -dynamiclib -framework IOKit -framework CoreFoundation -L$(BIN_TARGET) -lirtrcx
  CCOPT = $(CFLAGS) $(LFLAGS)
endif

.EXPORT_ALL_VARIABLES :

default: all
all: $(JIRTRCX)
	#@echo Built $(JIRTRCX)

$(JIRTRCX): Tower.cpp
	$(CXX) $(CCOPT) $(TRACE) -o "$@" *.cpp

clean:
	rm -rf *.o
	rm -f $(JIRTRCX)
