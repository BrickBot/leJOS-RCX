LEJOSHOME = ../..

LEJOSC = $(LEJOSHOME)/bin/lejosc
LEJOS = $(LEJOSHOME)/bin/lejos
EMULEJOS = $(LEJOSHOME)/bin/emu-lejos
LEJOSRUN = $(LEJOSHOME)/bin/lejosrun
EMULEJOSRUN = $(LEJOSHOME)/bin/emu-lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/lejosfirmdl

CLASSPATH = .
CLASSLIB = $(LEJOSHOME)/lib/classes.jar

SOURCES = Main.java
OBJECTS = Main.class
PROGRAMS = Main
BINARY = Main.bin
EMUBINARY = Main.emu
SIGNATURE = Main.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : run

run : $(BINARY)
	$(LEJOSRUN) $(BINARY)

emulate : $(EMUBINARY)
	$(EMULEJOSRUN) $(EMUBINARY)

firmdl :
	$(LEJOSFIRMDL)

$(BINARY) : $(OBJECTS)
	$(LEJOS) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(EMUBINARY) : $(OBJECTS)
	$(EMULEJOS) $(PROGRAMS) -o $(EMUBINARY)

$(OBJECTS) : $(SOURCES) $(CLASSLIB)
	$(LEJOSC) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
