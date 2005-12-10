# Makefile
# Adapted from config.mk in librcx by Kekoa Proudfoot.
#
# 2005.11.21 - Matthias Paul Scholz
#	- fixed

include $(LEJOS_HOME)/Makefile.config

PLAT_RCX_SRC=$(LEJOS_HOME)/src/platform/rcx

ifneq (,$(findstring cygwin,$(OSTYPE)))
  LEJOS_HOME_NATIVE=$(shell cygpath -m $(LEJOS_HOME))
  PLAT_RCX_SRC_NATIVE=$(shell cygpath -m $(PLAT_RCX_SRC))
  EXT=.exe
else
  LEJOS_HOME_NATIVE=$(LEJOS_HOME)
  PLAT_RCX_SRC_NATIVE=$(PLAT_RCX_SRC)
  EXT=
endif

CC = $(BINDIR)/$(BINPREFIX)gcc -c
LD = $(BINDIR)/$(BINPREFIX)ld -u__start --oformat coff-h8300
OC = $(BINDIR)/$(BINPREFIX)objcopy -I coff-h8300 -O srec

FASTDLPATH = $(BIN_TARGET)

default: all

all: fastdl2x.srec fastdl4x.srec install
	@echo Done building fastdl images

install:
	rm -f $(BIN_TARGET)/fastdl2x.srec $(BIN_TARGET)/fastdl4x.srec
	cp fastdl2x.srec $(BIN_TARGET)
	cp fastdl4x.srec $(BIN_TARGET)

fastdl2x.o:
	$(CC) fastdl.S -o fastdl2x.o -DDISABLE_COMPLEMENTS

fastdl4x.o:
	$(CC) fastdl.S -o fastdl4x.o -DDISABLE_COMPLEMENTS -DDOUBLE_BAUD_RATE

%.srec: %.o $(PLAT_RCX_SRC)/rcx.lds
	rm -f *.map *.lds
	$(LD) $(LFLAGS) -T$(PLAT_RCX_SRC)/rcx.lds -Map rcx.map -o $@ $<
	$(OC) $@
	${JAVA} -classpath "$(LEJOS_HOME_NATIVE)/release/lib/jtools.jar" js.tools.CodePacker $(PLAT_RCX_SRC_NATIVE)/rcx.lds rcx.map rcx.lds
	$(LD) $(LFLAGS) -Trcx.lds -o $@
	$(OC) $@

clean:
	rm -f *.o *.map *.lds *.srec
