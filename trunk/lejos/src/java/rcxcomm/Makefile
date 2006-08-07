JAVAC=javac -target 1.1 -source 1.2

rcx_classes:
	${JAVAC} -classpath "../../../lib/classes.jar" `find . -name '*.java'`
	jar cf ../../../lib/rcxcomm.jar `find . -name '*.class'`

