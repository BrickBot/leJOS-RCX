# Makefile
# Adapted from config.mk in librcx by Kekoa Proudfoot.

ifneq (,$(findstring cygwin,$(OSTYPE)))
  LEJOS_HOME_NATIVE=$(shell cygpath -m $(LEJOS_HOME))
else
  LEJOS_HOME_NATIVE=$(LEJOS_HOME)
endif

LIBRCX_HOME=./librcx
VMSRC=$(LEJOS_HOME)/src/javavm

include $(LEJOS_HOME)/Makefile.config

#CFLAGS += -I${LIBRCX_HOME}/lib -I$(VMSRC) -I. -O2 -Wall
CFLAGS += -I${LIBRCX_HOME}/lib -I$(VMSRC) -I. -O2 -Wall -fomit-frame-pointer
LFLAGS += -L${LIBRCX_HOME}/lib --relax -L${LIBRCX_HOME}/float -L/usr/local/h8300-hms/lib

LIBS = -lrcx -lfloat -lc

CC = $(BINDIR)/$(BINPREFIX)gcc
AS = $(BINDIR)/$(BINPREFIX)as
AR = $(BINDIR)/$(BINPREFIX)ar
LD = $(BINDIR)/$(BINPREFIX)ld -u__start --oformat coff-h8300
OC = $(BINDIR)/$(BINPREFIX)objcopy -I coff-h8300 -O srec

# other variables

COFF = --oformat coff-h8300
TINYVMBINARY = $(BIN_TARGET)/lejos.srec

# special targets

.PHONY: all clean
.PRECIOUS: %.o

default: ${TINYVMBINARY}

# The following should be used in all impl Makefiles.
# VMSRC and CFLAGS must be defined.

include $(VMSRC)/Makefile.include

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
       memory.o language.o native.o main.o sensors.o systime.o llc.o

all: ${PROGS}
	@echo Done building TinyVM srec

rcx.map: rcx.lds $(OBJS)
	@echo ""
	@echo "====> Building lejos.srec"
	@echo ""
	$(LD) $(LFLAGS) -Trcx.lds -Map rcx.map -o ${TINYVMBINARY} $(OBJS) $(LIBS)
	$(OC) ${TINYVMBINARY}

rcx_gen.lds: rcx.map
	@echo ""
	@echo "====> Packing lejos.srec"
	@echo ""
	${JAVA} -classpath "$(LEJOS_HOME_NATIVE)/release/lib/jtools.jar" js.tools.CodePacker rcx.lds rcx.map rcx_gen.lds

lib:
	@echo ""
	@echo "====> Making librcx"
	@echo ""
	cd $(LIBRCX_HOME); $(MAKE)
	
clean:
	rm -f core ${TINYVMBINARY} *.o *~ *.bak rcx.map rcx_gen.lds
	cd $(LIBRCX_HOME); $(MAKE) clean

${TINYVMBINARY}: lib rcx_gen.lds
	$(LD) $(LFLAGS) -Trcx_gen.lds -o ${TINYVMBINARY} $(OBJS) $(LIBS)
	$(OC) ${TINYVMBINARY}
