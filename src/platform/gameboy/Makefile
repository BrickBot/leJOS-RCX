
VMSRC=../vmsrc
LD=link-gbz80
CC=lcc
CFLAGS=-I../vmsrc -I. -Wa-l -Wl-m -Wl-j -tempdir=/usr/tmp

GBLEJOS_OBJ=exceptions.o threads.o memory.o language.o interpreter.o native.o main.o

# EXTRADEP used in vmsrc/Makefile.include

EXTRADEP=platform_config.h

default: programs

programs: ${GBLEJOS_OBJ}

include ../vmsrc/Makefile.include

%.o: %.c platform_config.h
	${CC} ${CFLAGS} -c $*.c

clean:
	rm -rf ${VMSRC}/*.o *.o core 

