LEJOS_HOME = ../../..

COMPILE = $(LEJOS_HOME)/bin/lejosjc
LINK = $(LEJOS_HOME)/bin/lejoslink
DOWNLOAD = $(LEJOS_HOME)/bin/lejosdl
FIRMDL = $(LEJOS_HOME)/bin/firmdl

CLASSPATH = .

SOURCES = View.java MotorView.java SensorView.java BatteryView.java
OBJECTS = View.class MotorView.class SensorView.class BatteryView.class
PROGRAMS = View
BINARY = View.bin
SIGNATURE = View.sig

.EXPORT_ALL_VARIABLES :

.PHONY : run

default : run

run : $(BINARY)
	$(DOWNLOAD) $(BINARY)

firmdl :
	$(FIRMDL)

$(BINARY) : $(OBJECTS)
	$(LINK) -verbose $(PROGRAMS) -o $(BINARY) > $(SIGNATURE)

$(OBJECTS) : $(SOURCES)
	$(COMPILE) $(SOURCES)

clean :
	rm -f $(BINARY) $(SIGNATURE) *.class *~ core
	rm -rf build
