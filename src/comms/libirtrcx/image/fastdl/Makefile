# Makefile

include $(LEJOS_HOME)/Makefile.config

AS = $(BINDIR)/$(BINPREFIX)as
LD = $(BINDIR)/$(BINPREFIX)ld -u__start --oformat coff-h8300
OC = $(BINDIR)/$(BINPREFIX)objcopy -I coff-h8300 -O srec

# other variables
COFF = --oformat coff-h8300
FASTDLBINARY = $(LEJOS_HOME)/bin/fastdl.srec

all: ${FASTDLBINARY}
	@echo Done building fastdl srec

${FASTDLBINARY}:
	$(AS) -o fastdl.o fastdl.s
	$(LD) -o ${FASTDLBINARY} fastdl.o
	$(OC) ${FASTDLBINARY}

clean:
	rm -f ${FASTDLBINARY} *.o
	