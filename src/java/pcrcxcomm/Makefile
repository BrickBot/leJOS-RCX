JAVAC=javac

pcclasses:
	${JAVAC} `find . -name '*.java'`
	jar cf ../../lib/pcrcxcomm.jar `find . -name '*.class'`
	cp `find . -name 'DataPort*.class'` ../rcxclasses/josx/rcxcomm

