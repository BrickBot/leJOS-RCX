JAVAC=javac

vision:
	${JAVAC} josx/vision/*.java
	jar cf ../lib/vision.jar josx/vision/*.class

javadoc:
	javadoc -protected -windowtitle "leJOS Vision API documentation" -author -d ../visionapidocs -sourcepath . josx.vision
