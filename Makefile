
SHELL=/bin/sh
JTOOLS=${TINYVM_HOME}/jtools
CLASSPATH=${JTOOLS}
COMMON_FOLDER=${TINYVM_HOME}/common
CLASSES_DIR=${TINYVM_HOME}/classes
LIB_DIR=${TINYVM_HOME}/lib

JAVAC=javac
JAVA=java

export CLASSPATH

all: check all_jtools tinyvm_bin

check:
	@if [ -f ${TINYVM_HOME} ]; then \
	  echo "Error: Please define TINYVM_HOME."; \
	  exit 1; \
	fi;

all_jtools: java_tools generated_files java_loader
	cd ${JTOOLS}; jar cf ${LIB_DIR}/jtools.jar `find . -name '*.class' -printf "%h/%f " `

java_tools:
	@echo Making tools
	${JAVAC} ${JTOOLS}/js/tools/*.java

generated_files: ${COMMON_FOLDER}/classes.db ${COMMON_FOLDER}/signatures.db
	${JAVA} -Dtinyvm.home=${TINYVM_HOME} js.tools.GenerateConstants

java_loader:
	@echo Making loader
	${JAVAC} ${JTOOLS}/js/tinyvm/*.java

tinyvm_bin:
	@echo Making TinyVM binary

core_classes:
	${JAVAC} -classpath ${CLASSES_DIR} `find ${CLASSES_DIR} -name '*.java' -printf "%h/%f " `
	cd ${CLASSES_DIR}; jar cf ${LIB_DIR}/classes.jar `find . -name '*.class' -printf "%h/%f " `

clean:
	rm -rf `find . -name '*.class' -printf "%h/%f "`
	rm -rf `find . -name 'core' -printf "%h/%f "`
	rm -rf `find . -name '*.o' -printf "%h/%f "`
	rm -rf `find . -name '*~' -printf "%h/%f "`








