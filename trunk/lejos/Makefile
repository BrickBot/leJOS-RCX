
SHELL=/bin/sh
CLASSPATH=jtools

JAVAC=jikes -bootclasspath c:/jdk1.3/jre/lib/rt.jar
#JAVAC=javac
JAVADOC=javadoc
JAVA=java
TEMP=/usr/tmp

export CLASSPATH

default: check all_jtools all_ctools core_classes tinyvm_emul
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
	cd ${TEMP}; jar cvf ${TINYVM_VERSION}.zip lejos
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

javadoc:
	if [ ! -d apidocs ]; then mkdir apidocs; fi
	${JAVADOC} -author -d apidocs -sourcepath classes java.io java.lang java.util josx.platform.rcx

clean:
	rm -rf `find . -name '*.class'`
	rm -rf `find . -name 'core'`
	rm -rf `find . -name '*.o'`
	rm -rf `find . -name '*~'`
	rm -rf `find . -name '*.bak'`
	rm -rf `find . -name '*.stackdump'`

distclean: clean
	rm -rf `find . -name 'CVS'`
	rm -rf `find . -name '.#*'`
	rm -rf `find . -name '*.tvm'`
	rm -rf `find . -name '*.bin'`
	rm -rf `find . -name '*.core'`
	rm -rf `find . -name '*.lst'`
	rm -rf `find . -name '*.log'`

distclean_src: distclean
	cd bin ; rm -f lejos lejosc lejosc1 lejosfirmdl lejosp lejosp1 lejosrun emu-dump emu-lejos emu-lejosrun
	cd unix_impl ; rm dump_config
	cd tools/firmdl ; rm mkimg
	rm -f bin/cygwin.dll
	rm -rf `find . -name '*.exe'`
	rm -rf `find . -name '*.jar'`

distclean_win: distclean
	/bin/strip `find . -name '*.exe'`

realclean: distclean_src
	rm -rf `find . -name '*.srec'`
