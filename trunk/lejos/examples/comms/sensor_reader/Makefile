LEJOSHOME = ../../..


LEJOSC = $(LEJOSHOME)/bin/lejosc
LEJOS = $(LEJOSHOME)/bin/lejos
LEJOSRUN = $(LEJOSHOME)/bin/lejosrun
LEJOSFIRMDL = $(LEJOSHOME)/bin/lejosfirmdl

RCXCLASSPATH = $(LEJOSHOME)/lib/classes.jar:$(LEJOSHOME)/lib/rcxrcxcomm.jar:.
RCXCLASSLIBS = $(LEJOSHOME)/lib/classes.jar $(LEJOSHOME)/lib/rcxrcxcomm.jar

RCXSOURCES = SensorReader.java
RCXOBJECTS = SensorReader.class
RCXPROGRAMS = SensorReader
RCXBINARY = SensorReader.bin
RCXSIGNATURE = SensorReader.sig


JAVAC = javac
JAVA = java

PCCLASSPATH = $(LEJOSHOME)/lib/pcrcxcomm.jar:.
PCCLASSLIBS = $(LEJOSHOME)/lib/pcrcxcomm.jar
PCLIBS = $(LEJOSHOME)/bin

PCSOURCES = ReadSensor.java
PCOBJECTS = ReadSensor.class
PCPROGRAMS = ReadSensor

.EXPORT_ALL_VARIABLES :

.PHONY : run

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
	$(JAVA) -classpath $(PCCLASSPATH) $(PCPROGRAMS)

$(PCOBJECTS) : $(PCSOURCES) $(PCCLASSLIB)
	$(JAVAC) -classpath $(PCCLASSPATH) $(PCOBJECTS)

clean :
	rm -f $(RCXBINARY) $(RCXSIGNATURE) *.class *~ core
