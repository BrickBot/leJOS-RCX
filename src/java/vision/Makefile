JAVAC=javac

ifneq (,$(findstring cygwin,$(OSTYPE)))
  PATH_SEP=;
else
  PATH_SEP=:
endif

vision:
	${JAVAC} -classpath "$(JMFHOME)/lib/jmf.jar$(PATH_SEP)../lib/pcrcxcomm.jar" josx/vision/*.java
	jar cf ../lib/vision.jar josx/vision/*.class

javadoc:
	javadoc -protected -windowtitle "leJOS Vision API documentation" -author -d ../visionapidocs -sourcepath . josx.vision
