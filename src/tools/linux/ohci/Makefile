# Makefile for legousbtower
#
# use uname -r to get correct version

KERNDIR=/lib/modules/$(shell uname -r)

# this is slightly more brain-dead, but works..
ifeq ($(shell if test -d $(KERNDIR)/build; then echo yes; fi),yes)
KERNINC=$(KERNDIR)/build/include
else
KERNINC=/usr/src/linux/include
endif

CFLAGS= -D__KERNEL__ -Wall -Wstrict-prototypes -Wno-trigraphs -O2 -fno-strict-aliasing -fno-common -fomit-frame-pointer -pipe -mpreferred-stack-boundary=2 -march=i686 -DMODULE -DMODVERSIONS -include $(KERNINC)/linux/modversions.h  -nostdinc -iwithprefix include -DKBUILD_BASENAME=usb_ohci  -I$(KERNINC) -I/usr/src/linux/drivers/usb

all:    usb-ohci.o

usb-ohci.o : usb-ohci.c


clean:
	rm -f *.o *~

