# Makefile for legousbtower
#

KERNDIR=/lib/modules/$(shell uname -r)


# this is slightly more brain-dead, but works..
ifeq ($(shell if test -d $(KERNDIR)/build; then echo yes; fi),yes)
KERNINC=$(KERNDIR)/build/include
else
KERNINC=/usr/src/linux/include
endif

CFLAGS= -D__KERNEL__ -DMODULE -Wall -Wstrict-prototypes -O2 \
-fomit-frame-pointer -fno-strict-aliasing -pipe -fno-strength-reduce \
-falign-loops=2 -falign-jumps=2 -falign-functions=2 \
 -I$(KERNINC) -I$(KERNINC)/drivers/usb

all:	legousbtower.o

legousbtower.o : legousbtower.c

clean:
	rm -f *.o *~

install: legousbtower.o
	/sbin/modprobe -r legousbtower
	/sbin/insmod legousbtower.o
	mknod --mode=a=rw /dev/lego0 c 180 240		
