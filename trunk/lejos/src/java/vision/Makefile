JAVAC=javac

vision:
	${JAVAC} -classpath "../lib/pcrcxcomm.jar" josx/vision/*.java
	jar cf ../lib/vision.jar josx/vision/*.class

javadoc:
	javadoc -protected -windowtitle "leJOS Vision API documentation" -author -d ../visionapidocs -sourcepath . josx.vision
