
SHELL=/bin/sh
CLASSPATH=src/java/tools

# OSTYPE is not set by default on Mac OS X
ifndef OSTYPE
  OSTYPE = $(shell uname -s|awk '{print tolower($$0)}')
endif

JAVAC=javac -target 1.1 -source 1.2
JAVADOC=javadoc
JAVA=java
TEMP=/usr/tmp

LEJOS_HOME=$(shell pwd)

MFLAGS = OSTYPE=$(OSTYPE) LEJOS_HOME=$(LEJOS_HOME)

PC_JAVADOC_SOURCE="src/java/rcxcomm/classes"

ifneq (,$(findstring cygwin,$(OSTYPE)))
  PATH_SEP=;
else
  PATH_SEP=:
endif


JAVADOC_SOURCE="src/java/classes${PATH_SEP}src/java/rcxcomm/rcxclasses"
REGRESSION_SRC="test/regression"
JTOOLS_SRC="src/java/tools"
CORE_CLASSES_SRC="src/java/classes"
VISION_SRC="src/java/vision"
RCXCOMM_SRC="src/java/rcxcomm"
PCRCXCOMM_SRC="src/java/pcrcxcomm"
JVM_SRC="src/javavm"
EMU_SRC="src/tools/emu-lejos"
PLAT_RCX_SRC="src/platform/rcx"
PLAT_UNIX_SRC="src/platform/unix"
PLAT_GBOY_SRC="src/platform/gameboy"
IRTRCX_LIB_SRC=src/comms/libirtrcx
JIRTRCX_LIB_SRC=src/comms/libjirtrcx

export CLASSPATH
export JAVA

default: emulator irtrcx_libs core_classes rcx_comm all_jtools tinyvm_emul
	@echo ""
	@echo "====> Installation of leJOS done!"

all: default lejos_bin

release:
	$(MAKE) $(MFLAGS) clean
	rm -rf apidocs pcapidocs
	$(MAKE) $(MFLAGS) all
	$(MAKE) $(MFLAGS) visionapi
	$(MAKE) $(MFLAGS) visiondoc
	export TINYVM_VERSION=lejos_`cat VERSION`; $(MAKE) dir_and_zip

dir_and_zip:
	rm -rf ${TEMP}/${TINYVM_VERSION}
	mkdir ${TEMP}/${TINYVM_VERSION}
	tar cf - . | (cd ${TEMP}/${TINYVM_VERSION}; tar xfpB -)
	cd ${TEMP}/${TINYVM_VERSION}; make distclean_src
	cd ${TEMP}; tar cvf ${TINYVM_VERSION}.tar ${TINYVM_VERSION}; gzip ${TINYVM_VERSION}.tar
	$(MAKE) $(MFLAGS) javadoc
	$(MAKE) $(MFLAGS) pcjavadoc
	rm -rf ${TEMP}/${TINYVM_VERSION}.doc
	mkdir ${TEMP}/${TINYVM_VERSION}.doc
	tar cf - apidocs pcapidocs visionapidocs docs README RELEASENOTES CLICKME.html LICENSE ACKNOWLEDGMENTS Makefile | (cd ${TEMP}/${TINYVM_VERSION}.doc; tar xfpB -)
	cd ${TEMP}/${TINYVM_VERSION}.doc
	rm -f ${TEMP}/${TINYVM_VERSION}.doc/Makefile
	cd ${TEMP}; tar cvf ${TINYVM_VERSION}.doc.tar ${TINYVM_VERSION}.doc; gzip ${TINYVM_VERSION}.doc.tar
	diff bin/lejos.srec ${TEMP}/${TINYVM_VERSION}/bin/lejos.srec

release_win:
	$(MAKE) clean
	rm -rf apidocs pcapidocs visionapidocs
	$(MAKE) all
	$(MAKE) $(MFLAGS) visionapi
	$(MAKE) $(MFLAGS) visiondoc
	export TINYVM_VERSION=lejos_win32_`cat VERSION`; make dir_and_zip_win

dir_and_zip_win:
	rm -rf ${TEMP}/lejos
	mkdir ${TEMP}/lejos
	cp -r . ${TEMP}/lejos
	cd ${TEMP}/lejos; make distclean_win
	cp /bin/cygwin1.dll ${TEMP}/lejos/bin
	rm -f ${TINYVM_VERSION}.zip
	cd ${TEMP}; zip -r ${TINYVM_VERSION}.zip lejos
	$(MAKE) $(MFLAGS) javadoc
	$(MAKE) $(MFLAGS) pcjavadoc
	rm -f ${TEMP}/${TINYVM_VERSION}.doc.zip
	cd ..; zip -r ${TEMP}/${TINYVM_VERSION}.doc.zip lejos/apidocs lejos/pcapidocs lejos/visionapidocs lejos/docs lejos/README lejos/RELEASENOTES lejos/CLICKME.html lejos/LICENSE lejos/ACKNOWLEDGMENTS
	diff bin/lejos.srec ${TEMP}/lejos/bin/lejos.srec


check_release:
	@echo TINYVM_HOME=${TINYVM_HOME}
	@echo Location of lejosc=`which lejosc`
	@echo Location of lejos=`which lejos`
	$(MAKE) $(MFLAGS) 
	cd $(REGRESSION_SRC); ./run.sh

all_jtools: java_tools generated_files java_loader
	cd $(JTOOLS_SRC); jar cf ../../../lib/jtools.jar `find . -name '*.class'`

java_tools:
	@echo ""
	@echo "====> Making java tools"
	@echo ""
	${JAVAC} -classpath "./src/java/tools${PATH_SEP}./lib/pcrcxcomm.jar" $(JTOOLS_SRC)/js/tools/*.java

generated_files:
	@echo ""
	@echo "====> Generating constants"
	@echo "" 
	${JAVA} -classpath $(CLASSPATH) -Dtinyvm.home="./src" js.tools.GenerateConstants

java_loader:
	@echo ""
	@echo "====> Making loader/linker (lejos)"
	@echo ""
	${JAVAC} $(JTOOLS_SRC)/js/tinyvm/*.java

emulator:
	@echo ""
	@echo "====> Making Emulator emu-lejos"
	@echo ""
	chmod 775 $(LEJOS_HOME)/cctest.sh
	cd $(EMU_SRC); $(MAKE) $(MFLAGS)

irtrcx_libs:
	@echo ""
	@echo "====> Making IR RCX communication libraries"
	@echo ""
	cd $(IRTRCX_LIB_SRC); $(MAKE) $(MFLAGS)
	cd $(JIRTRCX_LIB_SRC); $(MAKE) $(MFLAGS)

lejos_bin:
	@echo "====> Making leJOS RCX binary (lejos.srec)"
	cd $(PLAT_RCX_SRC); $(MAKE) $(MFLAGS)

tinyvm_emul:
	@echo ""
	@echo "====> Making leJOS Unix binaries (lejos, for emulation)"
	@echo ""
	cd $(PLAT_UNIX_SRC); $(MAKE) $(MFLAGS)

core_classes:
	@echo ""
	@echo "====> Making core classes"
	@echo ""
	${JAVAC} -classpath  $(CORE_CLASSES_SRC) `find $(CORE_CLASSES_SRC) -name '*.java'`
	cd $(CORE_CLASSES_SRC); jar cf ../../../lib/classes.jar `find . -name '*.class'`

rcx_comm: core_classes
	@echo ""
	@echo "====> Making rcxcomm"
	@echo ""
	cd $(RCXCOMM_SRC); $(MAKE) $(MFLAGS)
	cd $(PCRCXCOMM_SRC); $(MAKE) $(MFLAGS)

visionapi:
	cd $(VISION_SRC); $(MAKE) $(MFLAGS)

javadoc:
	${JAVADOC} -protected -windowtitle "leJOS API documentation" -author -d apidocs -sourcepath $(JAVADOC_SOURCE) java.io java.lang java.util josx.platform.rcx josx.util josx.robotics josx.rcxcomm java.net javax.servlet.http

pcjavadoc:
	${JAVADOC} -protected -windowtitle "leJOS PC API documentation" -author -d pcapidocs -sourcepath $(PC_JAVADOC_SOURCE) josx.rcxcomm

visiondoc:
	javadoc -protected -windowtitle "leJOS Vision API documentation" -author -d visionapidocs -classpath "$(JMFHOME)/lib/jmf.jar${PATH_SEP}./lib/pcrcxcomm.jar" -sourcepath vision josx.vision

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
	-rm -rf ./apidocs ./pcapidocs ./visionapidocs

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
	rm -f bin/lejos bin/emu-dump bin/emu-lejos bin/emu-lejosrun
	rm -f src/java/tools/js/tinyvm/SpecialClassConstants.java src/java/tools/js/tinyvm/SpecialSignatureConstants.java src/javavm/specialclasses.h src/javavm/specialsignatures.h
	rm -f $(PLAT_UNIX_SRC)/dump_config $(PLAT_UNIX_SRC)/platform_config.h
	rm -f $(EMU_SRC)/mkimg
	rm -f `find . -name '*.so' -o -name '*.dylib' -o -name '*.jnilib' -o -name '*.dll'`
	rm -f bin/cygwin.dll
	rm -f `find . -name '.DS_Store'`	# Mac OS X Finder droppings
	rm -f `find . -name '*.exe'`
	rm -f `find . -name '*.jar'`

distclean_win: distclean
	/bin/strip `find . -name '*.exe'`

realclean: distclean_src
	rm -f `find bin -name '*.srec'`
