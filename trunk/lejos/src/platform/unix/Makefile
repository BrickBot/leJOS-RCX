
VMSRC=../vmsrc
LD=ld
CC=gcc
CFLAGS=-I../vmsrc -O2 -Wall -Wno-unused -DEMULATE

DUMP_OBJ=dump.o load.o

VMEMUL_OBJ=interpreter.o threads.o exceptions.o \
	   memory.o language.o \
           tvmemul.o traceemul.o nativeemul.o load.o opcodeinfo.o

DUMP_TARGET=../bin/dump
VMEMUL_TARGET=../bin/lejosrun-unix

default: programs

programs: ${VMEMUL_TARGET} ${DUMP_TARGET}

include ../vmsrc/Makefile.include

%.o: %.c
	${CC} ${CFLAGS} -c $*.c

${DUMP_TARGET}: ${DUMP_OBJ}
	${CC} ${DUMP_OBJ} -o ${DUMP_TARGET}

${VMEMUL_TARGET}: ${VMEMUL_OBJ}
	${CC} ${VMEMUL_OBJ} -o ${VMEMUL_TARGET}

clean:
	rm -rf ${VMSRC}/*.o *.o core ${DUMP_TARGET} ${VMEMUL_TARGET}

