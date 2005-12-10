# Makefile for librcx.a

# The contents of this file are subject to the Mozilla Public License
# Version 1.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
# http://www.mozilla.org/MPL/
# 
# Software distributed under the License is distributed on an "AS IS"
# basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
# License for the specific language governing rights and limitations
# under the License.
# 
# The Original Code is Librcx code, released February 9, 1999.
# 
# The Initial Developer of the Original Code is Kekoa Proudfoot.
# Portions created by Kekoa Proudfoot are Copyright (C) 1999
# Kekoa Proudfoot. All Rights Reserved.

ROOT = ..
include $(ROOT)/make/shared.mk

SOURCES = init.c memcpy.c memset.c malloc.c \
          rcall0.s rcall1.s rcall2.s rcall3.s rcall4.s \
          mulhi3.s divhi3.s modhi3.s udivhi3.s umodhi3.s \
          mulsi3.s divsi3.s modsi3.s udivsi3.s umodsi3.s \
          cmpsi2.s ucmpsi2.s

OBJECTS = $(SOURCES:.c=.o) $(SOURCES:.s=.o)

INCS = -I.

all: librcx.a

librcx.a: $(OBJECTS)

clean:
	rm -f *.o librcx.a
