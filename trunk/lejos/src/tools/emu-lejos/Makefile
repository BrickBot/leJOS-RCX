
EMU_NAME=emu-lejos
EMU_BIN=$(BIN_TARGET)/${EMU_NAME}

CC=$(shell $(LEJOS_HOME)/release/cctest.sh)

CCOPT=-I../vmsrc -Wall -Wno-unused -O2 -DWRITE_ORDER=\"LE\" -DLOADER_TOOL=\"emu-lejosrun\" -DLINKER_TOOL=\"emu-lejos\"


default: programs

programs: ${EMU_NAME}

%.o: %.c
	${CC} ${CCOPT} -c $*.c


${EMU_NAME}: javaexec.c util.o
	${CC} ${CCOPT} $(TRACE) -c javaexec.c
	${CC} javaexec.o util.o -o ${EMU_BIN}

clean:
	rm -rf *.o *~ 
