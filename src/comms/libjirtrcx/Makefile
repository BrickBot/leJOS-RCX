CC=$(shell ../../cctest.sh)

JDK=$(shell dirname $(shell dirname $(shell which java)))

OS=${OSTYPE}
ifeq ($(OSTYPE),linux-gnu)
OS=linux
endif

OSX_USB=

ifneq (,$(findstring cygwin,$(OSTYPE)))
  # Windows with CygWin installed
IRTOWER=../../bin/irtower.dll
  CCOPT=-mno-cygwin -I${JDK}/include -I${JDK}/include/win32 -Wl,--add-stdcall-alias -shared
  # May not be necessary if make/gnumake sets CXX up correctly
  CXX=${CC}
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
    CCOPT = -g -O ${JNILIB_TYPE} -framework IOKit -framework CoreFoundation -I/System/Library/Frameworks/JavaVM.framework/Headers
IRTOWER=../../bin/libirtower.jnilib 
    # CXX set by default on Mac OS X.
    OSX_USB=osx_usb.o
  else
    # Linux/Solaris/FreeBSD/other unices.
    IRTOWER=../../bin/libirtower.so
    CCOPT=-shared -fpic -I${JDK}/include -I${JDK}/include/$(OS) 
    # CXX=${CC}
  endif
endif

.EXPORT_ALL_VARIABLES :

# Use CXX to get correct linkage (need C++ libraries)
${IRTOWER}: irtower.cpp rcx_comm.cpp rcx_comm.h josx_rcxcomm_Tower.h $(OSX_USB)
	$(CXX) $(CCOPT) $(OSX_USB) -o $@ *.cpp
osx_usb.o: osx_usb.c
	$(CC) -c -framework IOKit -framework CoreFoundation -I/System/Library/Frameworks/JavaVM.framework/Headers osx_usb.c
clean:
	rm -rf *.o *~ core
	rm -f ${IRTOWER}
        
