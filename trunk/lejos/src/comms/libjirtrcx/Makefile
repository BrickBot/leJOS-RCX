CC=$(shell $(LEJOS_HOME)/cctest.sh)

JDK=$(shell dirname $(shell dirname $(shell which java)))


OS=${OSTYPE}
ifeq ($(OSTYPE),linux-gnu)
OS=linux
endif

IRTRCX_H=$(LEJOS_HOME)/src/comms/libirtrcx/rcx_comm.h
IRTRCX_INCL=$(LEJOS_HOME)/src/comms/libirtrcx

ifneq (,$(findstring cygwin,$(OSTYPE)))
    # Windows with CygWin installed
    IRTOWER=$(LEJOS_HOME)/bin/jirtrcx.dll
    CCOPT= -mno-cygwin -I$(IRTRCX_INCL) -I${JDK}/include -I${JDK}/include/win32 -Wl,--add-stdcall-alias -shared -L$(LEJOS_HOME)/bin -lirtrcx
else
ifneq (,$(findstring darwin,$(OSTYPE)))
    # Mac OS X
    # uname -r is 1.4 on 10.1, 5.x on following SU's (yeah, baby !) and 6.0 on Jaguar (10.2)
    OS_VERSION = $(shell uname -r | cut -f1 -d'.')
    ifneq (,$(findstring $(OS_VERSION),1)$(findstring $(OS_VERSION),5))
      JNILIB_TYPE = -bundle
    else
      # Jaguar onwards supports plain dylib JNI libraries
      JNILIB_TYPE = -dynamiclib
    endif
    LFLAGS = $(JNILIB_TYPE) -framework IOKit -framework CoreFoundation
    LFLAGS += -L$(LEJOS_HOME)/bin -lirtrcx
    # Mac OS X compiles PIC by default
    CFLAGS = -g -O -I$(IRTRCX_INCL) -I/System/Library/Frameworks/JavaVM.framework/Headers 
    CCOPT = $(CFLAGS) $(LFLAGS)
    IRTOWER=$(LEJOS_HOME)/bin/libjirtrcx.jnilib 
  else
    # Linux/Solaris/FreeBSD/other unices.
    IRTOWER=$(LEJOS_HOME)/bin/libjirtrcx.so
    CCOPT=-rdynamic -ldl -shared -fpic -I${JDK}/include -I${JDK}/include/$(OS) -I$(IRTRCX_INCL) -I$(LEJOS_HOME)/src/tools/linux/legousbtower	

  endif
endif

.EXPORT_ALL_VARIABLES :

# Use CXX to get correct linkage (need C++ libraries)
${IRTOWER}: irtower.cpp josx_rcxcomm_Tower.h
	$(CXX) $(CCOPT) $(COMM_OBJ) -o $@ *.cpp

clean:
	rm -rf *.o *~ core
	rm -f ${IRTOWER}


