#
# Full paths work best inside emacs
#

PWD = $(shell pwd)
SRCS = $(addprefix $(PWD)/, tower.c)

CC=$(shell $(LEJOS_HOME)/release/cctest.sh)

JAVA_INCL="$(JAVA_HOME)"/include
IRTRCX_H=$(LEJOS_HOME)/src/comms/libirtrcx/rcx_comm.h
IRTRCX_INCL="$(LEJOS_HOME)/src/comms/libirtrcx"

# Default: Unix
JIRTRCX = $(BIN_TARGET)/libjirtrcx.so
CCOPT = -Wall -rdynamic -ldl -shared -fpic \
	-I${JAVA_INCL} -I${JAVA_INCL}/$(OSTYPE) -I$(IRTRCX_INCL) \
	-L$(BIN_TARGET) -lirtrcx

# Windows with CygWin installed
ifneq (,$(findstring cygwin,$(OSTYPE)))
  JIRTRCX=$(BIN_TARGET)/jirtrcx.dll
  CCOPT= -mno-cygwin -I${JAVA_INCL} -I${JAVA_INCL}/win32 -I$(IRTRCX_INCL) -Wl,--add-stdcall-alias -shared -L"$(BIN_TARGET)" -lirtrcx
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

all: ${JIRTRCX}

${JIRTRCX}: tower.c
	$(CC) $(CCOPT) $(TRACE) -o "$@" *.c

clean:
	rm -rf *.o
	rm -f ${JIRTRCX}
