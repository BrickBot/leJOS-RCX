LEJOSHOME = ../..

LEJOSC = $(LEJOSHOME)/bin/lejosc
LEJOS = $(LEJOSHOME)/bin/lejos
LEJOSRUN = $(LEJOSHOME)/bin/lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/lejosfirmdl

CLASSPATH = .

SOURCES = PerformanceTest.java
OBJECTS = PerformanceTest.class
PROGRAMS = PerformanceTest
BINARY = PerformanceTest.bin
SIGNATURE = PerformanceTest.sig

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
