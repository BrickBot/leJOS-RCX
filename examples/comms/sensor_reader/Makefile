LEJOSHOME = ../../..

LEJOSC = $(LEJOSHOME)/bin/lejosc
LEJOS = $(LEJOSHOME)/bin/lejos
LEJOSRUN = $(LEJOSHOME)/bin/lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/lejosfirmdl

RCXCLASSLIB = $(LEJOSHOME)/lib/classes.jar $(LEJOSHOME)/lib/rcxrcxcomm.jar

RCXSOURCES = SensorReader.java
RCXOBJECTS = SensorReader.class
RCXPROGRAMS = SensorReader
RCXBINARY = SensorReader.bin
RCXSIGNATURE = SensorReader.sig

JAVAC = javac
JAVA = java

ifneq (,$(findstring $(OSTYPE),cygwin))
PCCLASSPATH = $(LEJOSHOME)/lib/pcrcxcomm.jar;.
else
PCCLASSPATH = $(LEJOSHOME)/lib/pcrcxcomm.jar:.
endif

PCCLASSLIB = $(LEJOSHOME)/lib/pcrcxcomm.jar
PCLIBS = $(LEJOSHOME)/bin

LD_LIBRARY_PATH = $(PCLIBS)

PCSOURCES = ReadSensor.java
PCOBJECTS = ReadSensor.class
PCPROGRAMS = ReadSensor

.EXPORT_ALL_VARIABLES :

.PHONY : rcxrun pcrun

default : rcxrun pcrun

rcxrun : $(RCXBINARY)
	$(LEJOSRUN) $(RCXBINARY)

firmdl :
	$(LEJOSFIRMDL)

$(RCXBINARY) : $(RCXOBJECTS)
	$(LEJOS) -verbose $(RCXPROGRAMS) -o $(RCXBINARY) > $(RCXSIGNATURE)

$(RCXOBJECTS) : $(RCXSOURCES) $(RCXCLASSLIB)
	$(LEJOSC) -target 1.1 $(RCXSOURCES)

pcrun : $(PCOBJECTS)
	$(JAVA) -classpath "$(PCCLASSPATH)" $(PCPROGRAMS)

$(PCOBJECTS) : $(PCSOURCES) $(PCCLASSLIB)
	$(JAVAC) -classpath "$(PCCLASSPATH)" $(PCSOURCES)

clean :
	rm -f $(RCXBINARY) $(RCXSIGNATURE) *.class *~ core
