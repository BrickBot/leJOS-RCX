
VMSRC=../vmsrc
LD=link-gbz80
CC=lcc
CFLAGS=-I../vmsrc -I. -Wa-l -Wl-m -Wl-j

GBLEJOS_OBJ=interpreter.o 

#threads.o exceptions.o \
#	   memory.o language.o
           
# EXTRADEP used in vmsrc/Makefile.include

EXTRADEP=platform_config.h

default: programs

programs: ${GBLEJOS_OBJ}

include ../vmsrc/Makefile.include

%.o: %.c platform_config.h
	${CC} ${CFLAGS} -c $*.c
	
clean:
	rm -rf ${VMSRC}/*.o *.o core 

