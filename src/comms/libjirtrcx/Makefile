ifneq (,$(findstring $(OSTYPE),darwin))
CC=cc
else
CC=gcc
endif

OS=${OSTYPE}
OSCYG=$(findstring $(OSTYPE),cygwin)
ifneq (,$(findstring $(OSTYPE),cygwin))
IRTOWER=../../bin/irtower.dll
CCOPT=-mno-cygwin -I${JDK}/include -I${JDK}/include/win32 -Wl,--add-stdcall-alias -shared -o ${IRTOWER}
else
IRTOWER=../../bin/libirtower.so
CCOPT=-o ${IRTOWER} -shared -fpic -I${JDK}/include -I${JDK}/include/$(OSTYPE) 
endif

ifeq (,$(findstring $(OSTYPE),cygwin))
LDCONFIG=ldconfig ../../bin
endif

.PHONY : ldconfig


${IRTOWER}: irtower.cpp rcx_comm.cpp rcx_comm.h josx_rcxcomm_irTower.h
	$(CC) $(CCOPT) *.cpp

ldconfig:
	${LDCONFIG}

clean:
	rm -rf *.o *~ core
