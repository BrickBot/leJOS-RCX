LEJOSHOME = ../..

LEJOSC = $(LEJOSHOME)/bin/lejosc
LEJOS = $(LEJOSHOME)/bin/lejos
LEJOSRUN = $(LEJOSHOME)/bin/lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/lejosfirmdl

CLASSLIB = $(LEJOSHOME)/lib/classes.jar

CLASSPATH = .

SOURCES = SerialListenerTest.java
OBJECTS = SerialListenerTest.class
PROGRAMS = SerialListenerTest
BINARY = SerialListenerTest.bin
SIGNATURE = SerialListenerTest.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : run

run : $(BINARY)
	$(LEJOSRUN) $(BINARY)

firmdl :
	$(LEJOSFIRMDL)

fastfirmdl :
	$(LEJOSFIRMDL) -f

$(BINARY) : $(OBJECTS)
	$(LEJOS) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(OBJECTS) : $(SOURCES) $(CLASSLIB)
	$(LEJOSC) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
