
EMU_NAME=emu-lejos
EMU_BIN=$(LEJOS_HOME)/bin/${EMU_NAME}

CC=$(shell ../../../cctest.sh)

CCOPT=-I../vmsrc -Wall -Wno-unused -O2 -DWRITE_ORDER=\"LE\" -DLOADER_TOOL=\"emu-lejosrun\" -DLINKER_TOOL=\"emu-lejos\"


default: programs

programs: ${EMU_NAME}

%.o: %.c
	${CC} ${CCOPT} -c $*.c


${EMU_NAME}: javaexec.c util.o
	${CC} ${CCOPT} -c javaexec.c
	${CC} javaexec.o util.o -o ${EMU_BIN}

clean:
	rm -rf *.o *~ core ${EMU_BIN}
