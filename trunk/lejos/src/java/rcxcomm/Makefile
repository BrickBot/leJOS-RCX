JAVAC=javac -target 1.1

rcx_classes:
	${JAVAC} -classpath "$(LEJOS_HOME)/lib/classes.jar" `find . -name '*.java'`
	jar cf $(LEJOS_HOME)/lib/rcxrcxcomm.jar `find . -name '*.class'`

