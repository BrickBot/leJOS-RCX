
SHELL=/bin/sh
CLASSPATH=jtools

JAVAC=javac
JAVADOC=javadoc
JAVA=java
TEMP=/usr/tmp

export CLASSPATH

default: check all_jtools all_ctools core_classes tinyvm_emul
	@echo "====> Installation of leJOS done!"

all: default lejos_bin

release:
	cvs commit
	cvs tag RELEASE_`cat VERSION`
	make release_no_tag

release_no_tag:
	make clean
	make all
	rm -rf ${TEMP}/lejos_*
	export TINYVM_VERSION=lejos_`cat VERSION`; make dir_and_zip

dir_and_zip:
	cp -r * ${TEMP}/${TINYVM_VERSION}
	rm -rf `find ${TEM{}/${TINYVM_VERSION} -name 'CVS'`
	cp -r * ${TEMP}/tinyvm_check
	cd ${TEMP}; jar cvf ${TINYVM_VERSION}.zip ${TINYVM_VERSION}
	rm -rf ${TEMP}/${TINYVM_VERSION}

check:
	@if [ "${TINYVM_HOME}" != "" ]; then \
	  echo "Note: The TINYVM_HOME variable is no longer needed"; \
	  exit 1; \
	fi;

check_release:
	@echo TINYVM_HOME=${TINYVM_HOME}
	@echo Location of lejosc=`which lejosc`
	@echo Location of lejos=`which lejos`
	make
	cd regression; ./run.sh

all_jtools: java_tools generated_files java_loader
	cd jtools; jar cf ../lib/jtools.jar `find . -name '*.class' -printf "%h/%f " `

java_tools:
	@echo "====> Making tools"
	${JAVAC} jtools/js/tools/*.java

generated_files: common/classes.db common/signatures.db
	${JAVA} -Dtinyvm.home="." js.tools.GenerateConstants

java_loader:
	@echo "====> Making loader/linker (lejos)"
	${JAVAC} jtools/js/tinyvm/*.java

all_ctools:
	cd tools; make

lejos_bin:
	@echo "====> Making leJOS RCX binary (lejos.srec)"
	cd rcx_impl; make

tinyvm_emul:
	@echo "====> Making leJOS Unix binaries (lejos, for emulation)"
	cd unix_impl; make

core_classes:
	${JAVAC} -classpath classes `find classes -name '*.java'`
	cd classes; jar cf ../lib/classes.jar `find . -name '*.class'`

javadoc:
	if [ ! -d apidocs ]; then mkdir apidocs; fi
	${JAVADOC} -author -d apidocs -sourcepath classes java.io java.lang java.util josx.platform.rcx

clean:
	rm -rf `find . -name '*.class'`
	rm -rf `find . -name 'core'`
	rm -rf `find . -name '*.o'`
	rm -rf `find . -name '*~'`

cleaner: clean
	rm -rf `find . -name '*.srec'`






