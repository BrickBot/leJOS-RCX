ifneq (,$(findstring $(OSTYPE),darwin))
CC=cc
else
CC=gcc
endif

JDK=$(shell dirname $(shell dirname $(shell which java)))

OS=${OSTYPE}
ifeq ($(OSTYPE),linux-gnu)
OS=linux
endif
OSCYG=$(findstring $(OSTYPE),cygwin)
ifneq (,$(findstring $(OSTYPE),cygwin))
IRTOWER=../../bin/irtower.dll
CCOPT=-mno-cygwin -I${JDK}/include -I${JDK}/include/win32 -Wl,--add-stdcall-alias -shared -o ${IRTOWER}
else
IRTOWER=../../bin/libirtower.so
CCOPT=-o ${IRTOWER} -shared -fpic -I${JDK}/include -I${JDK}/include/$(OS) 
endif

.EXPORT_ALL_VARIABLES :

${IRTOWER}: irtower.cpp rcx_comm.cpp rcx_comm.h josx_rcxcomm_irTower.h
	$(CC) $(CCOPT) *.cpp

clean:
	rm -rf *.o *~ core
