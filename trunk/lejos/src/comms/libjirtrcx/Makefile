CC=$(shell ../../cctest.sh)

JDK=$(shell dirname $(shell dirname $(shell which java)))

OS=${OSTYPE}
ifeq ($(OSTYPE),linux-gnu)
OS=linux
endif
OSCYG=$(findstring cygwin,$(OSTYPE))
ifneq (,$(findstring cygwin,$(OSTYPE)))
IRTOWER=../../bin/irtower.dll
CCOPT=-mno-cygwin -I${JDK}/include -I${JDK}/include/win32 -Wl,--add-stdcall-alias -shared -o ${IRTOWER}
else
IRTOWER=../../bin/libirtower.so
CCOPT=-o ${IRTOWER} -shared -fpic -I${JDK}/include -I${JDK}/include/$(OS) -DLINUX
endif
# This is for OS X
ifneq (,$(findstring darwin,$(OSTYPE)))
CCOPT= -o ${IRTOWER} -bundle -fPIC -I/System/Library/Frameworks/JavaVM.framework/Headers
IRTOWER=../../bin/libirtower.jnilib
endif

.EXPORT_ALL_VARIABLES :

${IRTOWER}: irtower.cpp rcx_comm.cpp rcx_comm.h josx_rcxcomm_Tower.h
	$(CC) $(CCOPT) *.cpp

clean:
	rm -rf *.o *~ core
