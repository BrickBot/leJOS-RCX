# Makefile
# Adapted from config.mk in librcx by Kekoa Proudfoot.
#
# 2005.11.21 - Matthias Paul Scholz
#	- fixed

include $(LEJOS_HOME)/Makefile.config

LIBIRTRCX_HOME=$(shell realpath ../libirtrcx)
FASTDL_HOME=$(shell realpath ./fastdl)

ifneq (,$(findstring cygwin,$(OSTYPE)))
  LEJOS_HOME_NATIVE=$(shell cygpath -m $(LEJOS_HOME))
  PLAT_RCX_SRC_NATIVE=$(shell cygpath -m $(PLAT_RCX_SRC))
  EXT=.exe
else
  LEJOS_HOME_NATIVE=$(LEJOS_HOME)
  PLAT_RCX_SRC_NATIVE=$(PLAT_RCX_SRC)
  EXT=
endif

CC=$(shell $(LEJOS_HOME)/release/cctest.sh)
LD=ld

SRCS = dump.c mkimg.c mkimgj.c srec.c util.c
OBJS = $(addsuffix .o, $(basename $(SRCS)))

all: tools firmdl

tools: $(OBJS)
	@echo ""
	@echo "====> Building tools"
	@echo ""
	$(CC) -o mkimg$(EXT) mkimg.o srec.o -lc
	$(CC) -o mkimgj$(EXT) mkimgj.o srec.o -lc
	$(CC) -o dump$(EXT) dump.o srec.o -lc

image:
	@echo ""
	@echo "====> Building fastdl.srec"
	@echo ""
	cd $(FASTDL_HOME); $(MAKE) $(MFLAGS) all
	mkimg $(BIN_TARGET)/fastdl.srec

firmdl: image
	$(CC) -o firmdl$(EXT) firmdl.c -lc
	
%.o: %.c
	$(CC) -c -o $@ $(CFLAGS) "$<"

clean:
	rm -f *.o *.map *.lds
