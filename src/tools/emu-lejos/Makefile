
TVMFIRMDL=../bin/lejosfirmdl
TVM=../bin/lejosrun
TVMC=../bin/lejosc
TVMP=../bin/lejosp
CC=gcc
CCOPT=-I../vmsrc -O2 -Wall -Wno-unused
TVMFIRMDL_OBJ=tvm_firmdl3.o util.o
TVMCDEFS=-DTOOL_NAME=\"javac\" -DTOOL_ALT_VAR=\"JAVAC\"
TVMPDEFS=-DTOOL_NAME=\"javap\" -DTOOL_ALT_VAR=\"JAVAP\"

default: programs

programs: ${TVMFIRMDL} ${TVM} ${TVMC} ${TVMP}

%.o: %.c
	${CC} ${CCOPT} -c $*.c

${TVMFIRMDL}: ${TVMFIRMDL_OBJ}
	${CC} ${TVMFIRMDL_OBJ} -o ${TVMFIRMDL}

${TVM}: tvm.o
	${CC} tvm.o -o ${TVM}

${TVMC}: jtoolexec.c util.o
	${CC} ${TVMCDEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMC}

${TVMP}: jtoolexec.c util.o
	${CC} ${TVMPDEFS} -c jtoolexec.c
	${CC} jtoolexec.o util.o -o ${TVMP}
	
clean:
	rm -rf *.o *~ core ${TVMFIRMDL} ${TVM}



