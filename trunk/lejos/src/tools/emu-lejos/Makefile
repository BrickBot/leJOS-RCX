
TVMFIRMDL=../bin/lejosfirmdl
TVM=../bin/lejosrun
TVMC=../bin/lejosc
TVMP=../bin/lejosp
RCXLOADERNAME=lejos
UNIXLOADERNAME=lejos-unix
RCXLOADER=../bin/${RCXLOADERNAME}
UNIXLOADER=../bin/${UNIXLOADERNAME}
CC=gcc
CCOPT=-I../vmsrc -O2 -Wall -Wno-unused
LIBS=
TVMFIRMDL_OBJ=tvm_firmdl3.o util.o
TVMCDEFS=-DTOOL_NAME=\"javac\" -DTOOL_ALT_VAR=\"JAVAC\"
TVMPDEFS=-DTOOL_NAME=\"javap\" -DTOOL_ALT_VAR=\"JAVAP\"

LEJOSRCXDEFS=-DWRITE_ORDER=\"BE\" -DLOADER_TOOL=\"lejosrun\" 

LEJOSUNIXDEFS=-DWRITE_ORDER=\"LE\" -DLOADER_TOOL=\"lejosrun-unix\"

default: programs

programs: ${TVMFIRMDL} ${TVM} ${TVMC} ${TVMP} ${RCXLOADER} ${UNIXLOADER}

%.o: %.c
	${CC} ${CCOPT} -c $*.c

${TVMFIRMDL}: ${TVMFIRMDL_OBJ}
	${CC} ${TVMFIRMDL_OBJ} -o ${TVMFIRMDL} ${LIBS}

${TVM}: tvm.o
	${CC} tvm.o -o ${TVM}

${TVMC}: jtoolexec.c util.o
	${CC} ${TVMCDEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMC}

${TVMP}: jtoolexec.c util.o
	${CC} ${TVMPDEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMP}

${RCXLOADER}: javaexec.c util.o
	${CC} ${LEJOSRCXDEFS} -c javaexec.c
	${CC} javaexec.o util.o -o ${RCXLOADER}

${UNIXLOADER}: javaexec.c util.o
	${CC} ${LEJOSUNIXDEFS} -c javaexec.c
	${CC} javaexec.o util.o -o ${UNIXLOADER}
	
clean:
	rm -rf *.o *~ core ${TVMFIRMDL} ${TVM}



