
TVMFIRMDL=${TINYVM_HOME}/bin/tvmfirmdl
TVM=${TINYVM_HOME}/bin/tvm
CC=gcc
CCOPT=-I${TINYVM_HOME}/vmsrc -O2 -Wall -Wno-unused

default: programs

programs: ${TVMFIRMDL} ${TVM}

%.o: %.c
	${CC} ${CCOPT} -c $*.c

${TVMFIRMDL}: tvmfirmdl.o
	${CC} tvmfirmdl.o -o ${TVMFIRMDL}

${TVM}: tvm.o
	${CC} tvm.o -o ${TVM}

clean:
	rm -rf *.o *~ core ${TVMFIRMDL} ${TVM}



