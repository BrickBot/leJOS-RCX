# Makefile
# Adapted from config.mk in librcx by Kekoa Proudfoot.

include $(LEJOS_HOME)/Makefile.config

LIBIRTRCX_HOME=$(shell realpath ../libirtrcx)
FASTDL_HOME=$(shell realpath ./fastdl)

ifeq (,$(findstring cygwin,$(OSTYPE)))
  LEJOS_HOME_NATIVE=$(shell cygpath -m $(LEJOS_HOME))
  PLAT_RCX_SRC_NATIVE=$(shell cygpath -m $(PLAT_RCX_SRC))
  EXT=.exe
else
  LEJOS_HOME_NATIVE=$(LEJOS_HOME)
  PLAT_RCX_SRC_NATIVE=$(PLAT_RCX_SRC)
  EXT=
endif

CC=$(shell $(LEJOS_HOME)/cctest.sh)
LD=ld

SRCS = dump.c mkimg.c mkimgj.c srec.c util.c
OBJS = $(addsuffix .o, $(basename $(SRCS)))

all: lib tools firmdl

lib:
	@echo ""
	@echo "====> Building libirtrcx"
	@echo ""
	cd $(LIBIRTRCX_HOME); $(MAKE) all

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
	cd $(FASTDL_HOME); $(MAKE) all
	mkimg $(LEJOS_HOME)/bin/fastdl.srec

firmdl: image
	$(CC) -o firmdl$(EXT) firmdl.c -lc
	
%.o: %.c
	$(CC) -c -o $@ $(CFLAGS) "$<"

clean:
	rm -f *.o *.map *.lds ${FASTDLBINARY}
