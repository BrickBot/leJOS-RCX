# Makefile for legousbtower
#
# use uname -r to get correct version

KERNEL_VER=$(uname -r)
LINUXSRC=/usr/src/linux

MODULE_DIR = /lib/modules/$(KERNEL_VER)/kernel

CFLAGS= -D__KERNEL__ -DMODULE -Wall -Wstrict-prototypes -O2 \
-fomit-frame-pointer -fno-strict-aliasing -pipe -fno-strength-reduce \
-falign-loops=2 -falign-jumps=2 -falign-functions=2 \
 -I$(LINUXSRC)/include -I$(LINUXSRC)/drivers/usb

all:	legousbtower.o

legousbtower.o : legousbtower.c

clean:
	rm -f *.o *~
	/sbin/modprobe -r legousbtower
	rm -f /dev/lego0

install: legousbtower.o
	/sbin/modprobe -r legousbtower
	/sbin/insmod legousbtower.o
	mknod --mode=a=rw /dev/lego0 c 180 240		
