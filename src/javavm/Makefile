
# Makefile
# Adapted from config.mk in librcx by Kekoa Proudfoot.

include ${TINYVM_HOME}/Makefile.config
include ${TINYVM_HOME}/Makefile.common

CFLAGS += -I${LIBRCX_HOME}/lib -O2 -Wall -Wno-unused
LFLAGS += -L${LIBRCX_HOME}/lib

LIBS = -lrcx

CC = $(BINDIR)/$(BINPREFIX)gcc
AS = $(BINDIR)/$(BINPREFIX)as
AR = $(BINDIR)/$(BINPREFIX)ar
LD = $(BINDIR)/$(BINPREFIX)ld -T${TINYVM_HOME}/vmsrc/rcx.lds -u__start

# other variables

COFF = --oformat coff-h8300

# special targets

.PHONY: all clean
.PRECIOUS: %.o

# pattern rules

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

TINYVMBINARY = ${TINYVM_HOME}/bin/tinyvm.srec
PROGS = ${TINYVMBINARY}
OBJS = interpreter.o threads.o exceptions.o \
       memory.o language.o native.o main.o

all: ${PROGS}
	@echo Done building TinyVM srec

clean:
	rm -f core *.srec *.o *~

${TINYVMBINARY}: $(OBJS)
	$(LD) $(LFLAGS) -o ${TINYVMBINARY} $(OBJS) $(LIBS)

%.srec: %.o
	$(LD) $(LFLAGS) -o $*.srec $(filter %.o,$^) $(LIBS)






