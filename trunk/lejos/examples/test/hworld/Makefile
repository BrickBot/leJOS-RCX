LEJOSHOME = ../../..

LEJOSC     = $(LEJOSHOME)/bin/lejosjc
LEJOSLINK = $(LEJOSHOME)/bin/lejoslink
LEJOSDL    = $(LEJOSHOME)/bin/lejosdl

EMULEJOS = $(LEJOSHOME)/bin/emu-lejos
EMULEJOSRUN = $(LEJOSHOME)/bin/emu-lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/firmdl

CLASSPATH = .
CLASSLIB = $(LEJOSHOME)/lib/classes.jar

SOURCES = HelloWorld.java
OBJECTS = HelloWorld.class
PROGRAMS = HelloWorld
BINARY = HelloWorld.bin
EMUBINARY = HelloWorld.emu
SIGNATURE = HelloWorld.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : run

run : $(BINARY)
	$(LEJOSDL) $(BINARY)

emulate : $(EMUBINARY)
	$(EMULEJOSDL) $(EMUBINARY)

firmdl :
	$(LEJOSFIRMDL)

$(BINARY) : $(OBJECTS)
	$(LEJOSLINK) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(EMUBINARY) : $(OBJECTS)
	$(EMULEJOS) $(PROGRAMS) -o $(EMUBINARY)

$(OBJECTS) : $(SOURCES) $(CLASSLIB)
	$(LEJOSC) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
