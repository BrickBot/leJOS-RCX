
SHELL=/bin/sh
JTOOLS=${TINYVM_HOME}/jtools
CLASSPATH=${JTOOLS}
COMMON_FOLDER=${TINYVM_HOME}/common
CLASSES_DIR=${TINYVM_HOME}/classes
LIB_DIR=${TINYVM_HOME}/lib

JAVAC=javac
JAVADOC=javadoc
JAVA=java
TEMP=/usr/tmp

export CLASSPATH

default: check all_jtools all_ctools core_classes tinyvm_emul all_ctools

all: default tinyvm_bin

release:
	make all
	cvs commit
	cvs tag RELEASE_`cat VERSION`
	rm -rf ${TEMP}/tinyvm_*
	export TINYVM_VERSION=tinyvm_`cat VERSION`; make dir_and_zip

dir_and_zip:
	cvs export -D tomorrow -d ${TEMP}/${TINYVM_VERSION} tinyvm
	cd ${TEMP}; zip -r ${TINYVM_VERSION}.zip ${TINYVM_VERSION}
	diff bin/tinyvm.srec ${TEMP}/${TINYVM_VERSION}/bin/tinyvm.srec

check:
	@if [ -f ${TINYVM_HOME} ]; then \
	  echo "Error: Please define TINYVM_HOME."; \
	  exit 1; \
	fi;

check_release:
	echo TINYVM_HOME=${TINYVM_HOME}
	echo Location of tvmc=`which tvmc`
	echo Location of tvmld=`which tvmld`
	which tvmc;
	make
	cd regression; ./run.sh

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

all_ctools:
	cd tools; make

tinyvm_bin:
	@echo Making TinyVM binary
	cd vmsrc; make

tinyvm_emul:
	@echo Making TinyVM binary for emulation
	cd vmtest; make

core_classes:
	${JAVAC} -classpath "${CLASSES_DIR}" `find ${CLASSES_DIR} -name '*.java' -printf "%h/%f " `
	cd ${CLASSES_DIR}; jar cf ${LIB_DIR}/classes.jar `find . -name '*.class' -printf "%h/%f " `

javadoc:
	if [ ! -d apidocs ]; then mkdir apidocs; fi
	${JAVADOC} -d apidocs -sourcepath "${CLASSES_DIR}" java.lang tinyvm.rcx

clean:
	rm -rf `find . -name '*.class' -printf "%h/%f "`
	rm -rf `find . -name 'core' -printf "%h/%f "`
	rm -rf `find . -name '*.o' -printf "%h/%f "`
	rm -rf `find . -name '*~' -printf "%h/%f "`

cleaner: clean
	rm -rf `find . -name '*.srec'`






