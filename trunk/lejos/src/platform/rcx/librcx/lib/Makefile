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
          rcall0.c rcall1.c rcall2.c rcall3.c rcall4.c \
          mulhi3.c divhi3.c modhi3.c udivhi3.c umodhi3.c \
          mulsi3.c divsi3.c modsi3.c udivsi3.c umodsi3.c \
          cmpsi2.c ucmpsi2.c

OBJECTS = $(SOURCES:.c=.o)

INCS = -I.

all: librcx.a

librcx.a: $(OBJECTS)

clean:
	rm -f $(OBJECTS) librcx.a
