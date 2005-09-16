#
# Makefile for jni adapter for librcx
#

CC=$(shell $(LEJOS_HOME)/cctest.sh)

JAVA_INCL="$(JAVA_HOME)"/include

IRTRCX_INCL=$(LEJOS_HOME)/src/comms/libirtrcx2
IRTRCX_H=$(IRTRCX_INCL)/rcx_comm.h

# Default: Unix
JIRTRCX=$(LEJOS_HOME)/bin/libjirtrcx.so
CCOPT=-rdynamic -ldl -shared -fpic -I${JAVA_INCL} -I${JAVA_INCL}/$(OSTYPE) -I$(IRTRCX_INCL) -L$(LEJOS_HOME)/bin -lirtrcx

# Windows with CygWin installed
ifneq (,$(findstring cygwin,$(OSTYPE)))
  JIRTRCX=$(LEJOS_HOME)/bin/jirtrcx.dll
  CCOPT= -mno-cygwin -I${JAVA_INCL} -I${JAVA_INCL}/win32 -I$(IRTRCX_INCL) -Wl,--add-stdcall-alias -shared -L$(LEJOS_HOME)/bin -lirtrcx
endif

# MacOS X
ifneq (,$(findstring darwin,$(OSTYPE)))
  JIRTRCX=$(LEJOS_HOME)/bin/libjirtrcx.jnilib 
  # Mac OS X compiles PIC by default
  CFLAGS = -g -O -I$(IRTRCX_INCL) -I/System/Library/Frameworks/JavaVM.framework/Headers -I$(IRTRCX_INCL) 
  LFLAGS = -dynamiclib -framework IOKit -framework CoreFoundation -L$(LEJOS_HOME)/bin -lirtrcx
  CCOPT = $(CFLAGS) $(LFLAGS)
endif

.EXPORT_ALL_VARIABLES :

default: all
all: ${JIRTRCX}
	@echo Built $(JIRTRCX)

${JIRTRCX}: Tower.cpp
	$(CXX) $(CCOPT) -DTRACE -o $@ *.cpp

clean:
	rm -rf *.o
	rm -f ${JIRTRCX}
