
# Makefile
# Adapted from config.mk in librcx by Kekoa Proudfoot.

include ../Makefile.config

#CFLAGS += -I${LIBRCX_HOME}/lib -I../vmsrc -I. -O2 -Wall
CFLAGS += -I${LIBRCX_HOME}/lib -I../vmsrc -I. -O2 -Wall -fomit-frame-pointer
LFLAGS += -L${LIBRCX_HOME}/lib -L${LIBRCX_HOME}/float

LIBS = -lrcx -lfloat

CC = $(BINDIR)/$(BINPREFIX)gcc
AS = $(BINDIR)/$(BINPREFIX)as
AR = $(BINDIR)/$(BINPREFIX)ar
LD = $(BINDIR)/$(BINPREFIX)ld -u__start

# other variables

COFF = --oformat coff-h8300
TINYVMBINARY = ../bin/lejos.srec

# special targets

.PHONY: all clean
.PRECIOUS: %.o

default: ${TINYVMBINARY}

# The following should be used in all impl Makefiles.
# VMSRC and CFLAGS must be defined.

VMSRC=../vmsrc
include ../vmsrc/Makefile.include

%.o: %.c
	$(CC) $(CFLAGS) $(INCS) -c $*.c
%.o: %.s
	$(AS) $*.s -o $*.o
%.s: %.c
	$(CC) $(CFLAGS) $(INCS) -S $*.c
%.a:
	rm -f $*.a
	$(AR) rc $*.a $(filter %.o,$^)
%.coff: %.o
	$(LD) $(LFLAGS) $(COFF) -o $*.coff $(filter %.o,$^) $(LIBS)


#####################################################################

PROGS = ${TINYVMBINARY}
OBJS = interpreter.o threads.o exceptions.o poll.o \
       memory.o language.o native.o main.o sensors.o systime.o

all: ${PROGS}
	@echo Done building TinyVM srec

clean:
	rm -f core ../bin/*.srec *.o *~

${TINYVMBINARY}: $(OBJS) rcx.lds
	$(LD) $(LFLAGS) -Trcx.lds -o ${TINYVMBINARY} $(OBJS) $(LIBS)





