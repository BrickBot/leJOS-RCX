CC=$(shell ../../cctest.sh)

JDK=$(shell dirname $(shell dirname $(shell which java)))


OS=${OSTYPE}
ifeq ($(OSTYPE),linux-gnu)
OS=linux
endif

OSX_USB=

# COMM_OSX_C=../../tools/firmdl/osx_usb.c
# COMM_OBJ=../../tools/firmdl/rcx_comm.o
# COMM_C=../../tools/firmdl/rcx_comm.c
# COMM_H=../../tools/firmdl/rcx_comm.h
# COMM_INCL=../../tools/firmdl
COMM_OBJ=
COMM_C=rcx_comm.cpp
COMM_H=rcx_comm.h
COMM_INCL=.

ifneq (,$(findstring cygwin,$(OSTYPE)))
    # Windows with CygWin installed
    IRTOWER=../../bin/irtower.dll
    CCOPT=-mno-cygwin -I$(COMM_INCL) -I${JDK}/include -I${JDK}/include/win32 -Wl,--add-stdcall-alias -shared
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
    # Mac OS X compiles PIC by default
    CCOPT = -g -O ${JNILIB_TYPE} -I$(COMM_INCL) -framework IOKit -framework CoreFoundation -I/System/Library/Frameworks/JavaVM.framework/Headers
    IRTOWER=../../bin/libirtower.jnilib 

    OSX_USB=osx_usb.o
  else
    # Linux/Solaris/FreeBSD/other unices.
    IRTOWER=../../bin/libirtower.so
    CCOPT=-shared -fpic -I${JDK}/include -I${JDK}/include/$(OS) -I$(COMM_INCL)

  endif
endif

.EXPORT_ALL_VARIABLES :

# Use CXX to get correct linkage (need C++ libraries)
${IRTOWER}: irtower.cpp $(COMM_C) $(COMM_H) josx_rcxcomm_Tower.h $(OSX_USB)
	$(CXX) $(CCOPT) $(OSX_USB) $(COMM_OBJ) -o $@ *.cpp
osx_usb.o: $(COMM_OSX_C)
	$(CC) -c -framework IOKit -framework CoreFoundation -I/System/Library/Frameworks/JavaVM.framework/Headers $(COMM_OSX_C)
clean:
	rm -rf *.o *~ core
	rm -f ${IRTOWER}


