LEJOSHOME = ../../..

LEJOSC = $(LEJOSHOME)/bin/lejosc
LEJOS = $(LEJOSHOME)/bin/lejos
LEJOSRUN = $(LEJOSHOME)/bin/lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/lejosfirmdl

CLASSPATH = .
CLASSLIB = $(LEJOSHOME)/lib/classes.jar

PCSOURCES = LNPAddrReadSensor.java

SOURCES = LNPAddrSensorReader.java
OBJECTS = LNPAddrSensorReader.class
PROGRAMS = LNPAddrSensorReader
BINARY = LNPAddrSensorReader.bin
SIGNATURE = LNPAddrSensorReader.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : pc run

pc :
	javac -classpath ${LEJOSHOME}/lib/pcrcxcomm.jar ${PCSOURCES}

run : $(BINARY)
	$(LEJOSRUN) $(BINARY)

firmdl :
	$(LEJOSFIRMDL)

$(BINARY) : $(OBJECTS)
	$(LEJOS) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(OBJECTS) : $(SOURCES) $(CLASSLIB)
	$(LEJOSC) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
