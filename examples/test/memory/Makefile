LEJOS_HOME = ../../..

COMPILE = $(LEJOS_HOME)/bin/lejosjc
LINK = $(LEJOS_HOME)/bin/lejoslink
DOWNLOAD = $(LEJOS_HOME)/bin/lejosdl
FIRMDL = $(LEJOS_HOME)/bin/firmdl

EMULINK = $(LEJOS_HOME)/bin/emu-lejos
EMULATE = $(LEJOS_HOME)/bin/emu-lejosrun

DOWNLOAD_OPTIONS = --tty=/dev/ttyS0

CLASSPATH = .
CLASSLIB = $(LEJOS_HOME)/lib/classes.jar

SOURCES = MemoryTest.java
OBJECTS = MemoryTest.class
PROGRAMS = MemoryTest
BINARY = MemoryTest.bin
EMUBINARY = MemoryTest.emu
SIGNATURE = MemoryTest.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : run

run : $(BINARY)
	$(DOWNLOAD) $(DOWNLOAD_OPTIONS) $(BINARY)

emulate : $(EMUBINARY)
	$(EMULATE) $(EMUBINARY)

firmdl :
	$(FIRMDL)

$(BINARY) : $(OBJECTS)
	$(LINK) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(EMUBINARY) : $(OBJECTS)
	$(EMULINK) $(PROGRAMS) -o $(EMUBINARY)

$(OBJECTS) : $(SOURCES) $(CLASSLIB)
	$(COMPILE) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
