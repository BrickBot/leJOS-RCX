LEJOS_HOME = ../../..

COMPILE = $(LEJOS_HOME)/bin/lejosjc
LINK = $(LEJOS_HOME)/bin/lejoslink
DOWNLOAD = $(LEJOS_HOME)/bin/lejosdl
FIRMDL = $(LEJOS_HOME)/bin/firmdl

DOWNLOAD_OPTIONS = --tty=/dev/ttyS0

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
	$(DOWNLOAD) $(DOWNLOAD_OPTIONS) $(BINARY)

firmdl :
	$(FIRMDL)

$(BINARY) : $(OBJECTS)
	$(LINK) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(OBJECTS) : $(SOURCES)
	$(COMPILE) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
