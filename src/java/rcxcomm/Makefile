JAVAC=javac -target 1.1

rcx_classes:
	${JAVAC} -classpath "../../../lib/classes.jar" `find . -name '*.java'`
	jar cf ../../../lib/rcxrcxcomm.jar `find . -name '*.class'`

