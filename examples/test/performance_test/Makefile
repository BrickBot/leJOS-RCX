LEJOS_HOME = ../../..

COMPILE = $(LEJOS_HOME)/bin/lejosjc
LINK = $(LEJOS_HOME)/bin/lejoslink
DOWNLOAD = $(LEJOS_HOME)/bin/lejosdl
FIRMDL = $(LEJOS_HOME)/bin/firmdl

EMULINK = $(LEJOS_HOME)/bin/emu-lejos
EMULATE = $(LEJOS_HOME)/bin/emu-lejosrun

DOWNLOAD_OPTIONS = --tty=/dev/ttyS0
FIRMDL_OPTIONS = --tty=/dev/ttyS0

CLASSPATH = .
CLASSLIB = $(LEJOS_HOME)/lib/classes.jar

SOURCES = PerformanceTest.java
OBJECTS = PerformanceTest.class
PROGRAMS = PerformanceTest
BINARY = PerformanceTest.bin
EMUBINARY = PerformanceTest.emu
SIGNATURE = PerformanceTest.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : run

run : $(BINARY)
	$(DOWNLOAD) $(DOWNLOAD_OPTIONS) $(BINARY)

emulate : $(EMUBINARY)
	$(EMULATE) $(EMUBINARY)

firmdl :
	$(FIRMDL) $(FIRMDL_OPTIONS)

$(BINARY) : $(OBJECTS)
	$(LINK) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(EMUBINARY) : $(OBJECTS)
	$(EMULINK) $(PROGRAMS) -o $(EMUBINARY)

$(OBJECTS) : $(SOURCES) $(CLASSLIB)
	$(COMPILE) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
