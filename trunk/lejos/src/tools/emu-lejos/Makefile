
TVMFIRMDL=../bin/aux-lejosfirmdl
TVM=../bin/lejosrun
CC=gcc
CCOPT=-I../vmsrc -O2 -Wall -Wno-unused

default: programs

programs: ${TVMFIRMDL} ${TVM}

%.o: %.c
	${CC} ${CCOPT} -c $*.c

${TVMFIRMDL}: tvm_firmdl3.o
	${CC} tvm_firmdl3.o -o ${TVMFIRMDL}

${TVM}: tvm.o
	${CC} tvm.o -o ${TVM}

clean:
	rm -rf *.o *~ core ${TVMFIRMDL} ${TVM}



