
SHELL=/bin/sh
CLASSPATH=jtools

#JAVAC=jikes -bootclasspath c:/jdk1.3/jre/lib/rt.jar
JAVAC=javac
JAVADOC=javadoc
JAVA=java
TEMP=/usr/tmp

export CLASSPATH

default: check all_jtools all_ctools core_classes rcx_comm tinyvm_emul
	@echo "====> Installation of leJOS done!"

all: default lejos_bin

release:
	make clean
	rm -rf apidocs
	make all
	export TINYVM_VERSION=lejos_`cat VERSION`; make dir_and_zip

dir_and_zip:
	rm -rf ${TEMP}/${TINYVM_VERSION}
	mkdir ${TEMP}/${TINYVM_VERSION}
	tar cf - . | (cd ${TEMP}/${TINYVM_VERSION}; tar xfpB -)
	cd ${TEMP}/${TINYVM_VERSION}; make distclean_src
	cd ${TEMP}; tar cvf ${TINYVM_VERSION}.tar ${TINYVM_VERSION}; gzip ${TINYVM_VERSION}.tar
	make javadoc
	rm -rf ${TEMP}/${TINYVM_VERSION}.doc
	mkdir ${TEMP}/${TINYVM_VERSION}.doc
	tar cf - apidocs docs README RELEASENOTES CLICKME.html LICENSE ACKNOWLEDGMENTS Makefile | (cd ${TEMP}/${TINYVM_VERSION}.doc; tar xfpB -)
	cd ${TEMP}/${TINYVM_VERSION}.doc; make distclean_src
	rm -f ${TEMP}/${TINYVM_VERSION}.doc/Makefile
	cd ${TEMP}; tar cvf ${TINYVM_VERSION}.doc.tar ${TINYVM_VERSION}.doc; gzip ${TINYVM_VERSION}.doc.tar
	diff bin/lejos.srec ${TEMP}/${TINYVM_VERSION}/bin/lejos.srec

release_win:
	make clean
	rm -rf apidocs
	make all
	export TINYVM_VERSION=lejos_win32_`cat VERSION`; make dir_and_zip_win

dir_and_zip_win:
	rm -rf ${TEMP}/lejos
	mkdir ${TEMP}/lejos
	cp -r . ${TEMP}/lejos
	cd ${TEMP}/lejos; make distclean_win
	cp /bin/cygwin1.dll ${TEMP}/lejos/bin
	rm -f ${TINYVM_VERSION}.zip
	cd ${TEMP}; zip -r ${TINYVM_VERSION}.zip lejos
	make javadoc
	rm -f ${TEMP}/${TINYVM_VERSION}.doc.zip
	cd ..; zip -r ${TEMP}/${TINYVM_VERSION}.doc.zip lejos/apidocs lejos/docs lejos/README lejos/RELEASENOTES lejos/CLICKME.html lejos/LICENSE lejos/ACKNOWLEDGEMENTS
	diff bin/lejos.srec ${TEMP}/lejos/bin/lejos.srec

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
	cd jtools; jar cf ../lib/jtools.jar `find . -name '*.class'`

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

rcx_comm:
	cd rcxcomm; make

javadoc:
	if [ ! -d apidocs ]; then mkdir apidocs; fi
	${JAVADOC} -windowtitle "leJOS API documentation" -author -d apidocs -sourcepath classes java.io java.lang java.util josx.platform.rcx josx.robotics

clean:
	rm -f `find . -name '*.class'`
	rm -f `find . -name 'core'`
	rm -f `find . -name '*.o'`
	rm -f `find . -name '*~'`
	rm -f `find . -name '*.tvm'`
	rm -f `find . -name '*.bin'`
	rm -f `find . -name '*.sig'`
	rm -f `find . -name '*.bak'`
	rm -f `find . -name '*.stackdump'`
	rm -f `find . -name '*.backtrace'`
	-rm -rf apidocs

distclean: clean
	rm -rf `find . -name 'CVS'`
	rm -f `find . -name '.#*'`
	rm -f `find . -name '*.tvm'`
	rm -f `find . -name '*.bin'`
	rm -f `find . -name '*.sig'`
	rm -f `find . -name '*.core'`
	rm -f `find . -name '*.lst'`
	rm -f `find . -name '*.log'`

distclean_src: distclean
	rm -f bin/lejos bin/lejosc bin/lejosc1 bin/lejosfirmdl bin/lejosp bin/lejosp1 bin/lejosrun bin/emu-dump bin/emu-lejos bin/emu-lejosrun
	rm -f unix_impl/dump_config
	rm -f tools/firmdl/mkimg
	rm -f bin/cygwin.dll
	rm -f `find . -name '*.exe'`
	rm -f `find . -name '*.jar'`

distclean_win: distclean
	/bin/strip `find . -name '*.exe'`

realclean: distclean_src
	rm -f `find . -name '*.srec'`