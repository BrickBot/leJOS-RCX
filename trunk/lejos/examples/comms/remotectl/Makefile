LEJOS_HOME = ../../..

# Program specific
PROGRAM = Remote
# PROGRAM = RemoteControlTest
HOST_PROGRAM = MessengerTest

# General
COMPILER = $(LEJOS_HOME)/bin/lejosjc
LINKER = $(LEJOS_HOME)/bin/lejoslink
LOADER = $(LEJOS_HOME)/bin/lejosdl
FIRMWARE_LOADER = $(LEJOS_HOME)/bin/firmdl

CLASSPATH = .
CLASSLIB = $(LEJOS_HOME)/lib/classes.jar

HOST_CLASSPATH = $(LEJOS_HOME)/lib/pcrcxcomm.jar:$(LEJOS_HOME)/bin:.
LD_LIBRARY_PATH := $(LD_LIBRARY_PATH):$(LEJOS_HOME)/bin
LD_PRELOAD = $(LEJOS_HOME)/bin/libirtrcx.so

SOURCES = $(PROGRAM).java
OBJECTS = $(PROGRAM).class
PROGRAMS = $(PROGRAM)
BINARY = $(PROGRAM).bin
SIGNATURE = $(PROGRAM).sig

HOST_SOURCES = $(HOST_PROGRAM).java
HOST_OBJECTS = $(HOST_PROGRAM).class

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

$(HOST_OBJECTS) : $(HOST_SOURCES)
	javac -classpath $(HOST_CLASSPATH) $(HOST_SOURCES)

host_run : $(HOST_OBJECTS)
	java -Djava.library.path=$(LEJOS_HOME)/bin -classpath $(HOST_CLASSPATH) $(HOST_PROGRAM)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
