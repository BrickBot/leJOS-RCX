# Makefile
# Adapted from config.mk in librcx by Kekoa Proudfoot.

include $(LEJOS_HOME)/Makefile.config

PLAT_RCX_SRC=$(LEJOS_HOME)/src/platform/rcx

ifeq (,$(findstring cygwin,$(OSTYPE)))
  LEJOS_HOME_NATIVE=$(shell cygpath -m $(LEJOS_HOME))
  PLAT_RCX_SRC_NATIVE=$(shell cygpath -m $(PLAT_RCX_SRC))
else
  LEJOS_HOME_NATIVE=$(LEJOS_HOME)
  PLAT_RCX_SRC_NATIVE=$(PLAT_RCX_SRC)
endif

AS = $(BINDIR)/$(BINPREFIX)as
LD = $(BINDIR)/$(BINPREFIX)ld -u__start --oformat coff-h8300
OC = $(BINDIR)/$(BINPREFIX)objcopy -I coff-h8300 -O srec

FASTDLBINARY = $(LEJOS_HOME)/bin/fastdl.srec

default: ${FASTDLBINARY}

all: ${FASTDLBINARY}
	@echo Done building fastdl.srec

rcx.map: $(PLAT_RCX_SRC)/rcx.lds
	@echo ""
	@echo "====> Building fastdl.srec"
	@echo ""
	$(AS) -o fastdl.o fastdl.s
	$(LD) $(LFLAGS) -T$(PLAT_RCX_SRC)/rcx.lds -Map rcx.map -o ${FASTDLBINARY} fastdl.o
	$(OC) ${FASTDLBINARY}

rcx_gen.lds: rcx.map
	@echo ""
	@echo "====> Packing fastdl.srec"
	@echo ""
	${JAVA} -classpath "$(LEJOS_HOME_NATIVE)/lib/jtools.jar" js.tools.CodePacker $(PLAT_RCX_SRC_NATIVE)/rcx.lds rcx.map rcx_gen.lds

${FASTDLBINARY}: rcx_gen.lds
	@echo ""
	@echo "====> Linking fastdl.srec"
	@echo ""
	$(LD) $(LFLAGS) -Trcx_gen.lds -o ${FASTDLBINARY}
	$(OC) ${FASTDLBINARY}

clean:
	rm -f *.o *.map *.lds ${FASTDLBINARY}
