LEJOSHOME = ../..

LEJOSC = $(LEJOSHOME)/bin/lejosc
LEJOS = $(LEJOSHOME)/bin/lejos
LEJOSRUN = $(LEJOSHOME)/bin/lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/lejosfirmdl

CLASSPATH = .

SOURCES = View.java \
	LCDNumber.java LCDProgramNumber.java LCDSegment.java \
	MotorView.java PortView.java SensorView.java
OBJECTS = View.class \
	LCDNumber.class LCDProgramNumber.class LCDSegment.class \
	MotorView.class PortView.class SensorView.class
PROGRAMS = View
BINARY = View.bin
SIGNATURE = View.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : run

run : $(BINARY)
	$(LEJOSRUN) $(BINARY)

firmdl :
	$(LEJOSFIRMDL)

$(BINARY) : $(OBJECTS)
	$(LEJOS) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(OBJECTS) : $(SOURCES)
	$(LEJOSC) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
