LEJOS_HOME = ../../../..

# Program specific
PROGRAM = PingPong

# General
COMPILER = $(LEJOS_HOME)/bin/lejosjc
LINKER = $(LEJOS_HOME)/bin/lejoslink
LOADER = $(LEJOS_HOME)/bin/lejosdl
FIRMWARE_LOADER = $(LEJOS_HOME)/bin/firmdl

CLASSPATH = .
CLASSLIB = $(LEJOS_HOME)/lib/classes.jar

SOURCES = $(PROGRAM).java
OBJECTS = $(PROGRAM).class
PROGRAMS = $(PROGRAM)
BINARY = $(PROGRAM).bin
SIGNATURE = $(PROGRAM).sig

.EXPORT_ALL_VARIABLES :

.PHONY : load firmdl host_run clean


default : load


load : $(BINARY)
	$(LOADER) $(BINARY)

firmdl :
	$(FIRMWARE_LOADER)

$(BINARY) : $(OBJECTS)
	$(LINKER) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(OBJECTS) : $(SOURCES) $(CLASSLIB)
	$(COMPILER) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
