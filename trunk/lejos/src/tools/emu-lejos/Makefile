
TVMFIRMDL=../bin/lejosfirmdl
TVM=../bin/lejosrun
TVMC=../bin/lejosc
TVMP=../bin/lejosp
TVMC1=../bin/lejosc1
TVMP1=../bin/lejosp1
RCXLOADERNAME=lejos
UNIXLOADERNAME=emu-lejos
RCXLOADER=../bin/${RCXLOADERNAME}
UNIXLOADER=../bin/${UNIXLOADERNAME}

TVM_OBJ=lejosrun.o firmdl/rcx_comm.o
CC=$(shell ../cctest.sh)

OS=$(shell echo $$OSTYPE)

CCOPT=-I../vmsrc -Wall -Wno-unused -O2 

LIBS=
ifneq (,$(findstring darwin,$(OSTYPE)))
	LIBS += -framework IOKit -framework CoreFoundation
        TVM_OBJ += firmdl/osx_usb.o
endif

TVMFIRMDL_OBJ=tvm_firmdl3.o util.o
TVMCDEFS=-DTOOL_NAME=\"javac\" -DTOOL_ALT_VAR=\"JAVAC\" -DJAVA2
TVMPDEFS=-DTOOL_NAME=\"javap\" -DTOOL_ALT_VAR=\"JAVAP\" -DJAVA2
TVMC1DEFS=-DTOOL_NAME=\"javac\" -DTOOL_ALT_VAR=\"JAVAC\"
TVMP1DEFS=-DTOOL_NAME=\"javap\" -DTOOL_ALT_VAR=\"JAVAP\"

LEJOSRCXDEFS=-DWRITE_ORDER=\"BE\" -DLOADER_TOOL=\"lejosrun\" -DLINKER_TOOL=\"lejos\"
 
LEJOSUNIXDEFS=-DWRITE_ORDER=\"LE\" -DLOADER_TOOL=\"emu-lejosrun\" -DLINKER_TOOL=\"emu-lejos\"

PROGRAMS=${TVM} ${TVMC} ${TVMP} ${TVMC1} ${TVMP1} ${RCXLOADER} ${UNIXLOADER}

default: firmdl programs

firmdl: FORCE
	cd firmdl; $(MAKE)

FORCE:

programs: ${PROGRAMS}

%.o: %.c
	${CC} ${CCOPT} -c $*.c

${TVMFIRMDL}: ${TVMFIRMDL_OBJ}
	${CC} ${TVMFIRMDL_OBJ} -o ${TVMFIRMDL} ${LIBS}

lejosrun.o: lejosrun.c ../vmsrc/magic.h

${TVM}: lejosrun.o firmdl
	${CC} ${TVM_OBJ} -o ${TVM} ${LIBS}

${TVMC}: jtoolexec.c util.o
	${CC} ${TVMCDEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMC}

${TVMP}: jtoolexec.c util.o
	${CC} ${TVMPDEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMP}

${TVMC1}: jtoolexec.c util.o
	${CC} ${TVMC1DEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMC1}

${TVMP1}: jtoolexec.c util.o
	${CC} ${TVMP1DEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMP1}

${RCXLOADER}: javaexec.c util.o
	${CC} ${LEJOSRCXDEFS} -c javaexec.c
	${CC} javaexec.o util.o -o ${RCXLOADER}

${UNIXLOADER}: javaexec.c util.o
	${CC} ${LEJOSUNIXDEFS} -c javaexec.c
	${CC} javaexec.o util.o -o ${UNIXLOADER}

clean:
	rm -rf *.o *~ core ${PROGRAMS}
	cd firmdl; $(MAKE) clean
