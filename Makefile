
SHELL=/bin/sh

# OSTYPE is not set by default on Mac OS X
ifndef OSTYPE
  OSTYPE = $(shell uname -s|awk '{print tolower($$0)}')
endif

ANT=ant
TEMP=/usr/tmp

LEJOS_HOME=$(shell pwd)

MFLAGS = OSTYPE=$(OSTYPE) LEJOS_HOME=$(LEJOS_HOME)

ifneq (,$(findstring cygwin,$(OSTYPE)))
  PATH_SEP=;
else
  PATH_SEP=:
endif


REGRESSION_SRC="test/regression"
JVM_SRC="src/javavm"
EMU_SRC="src/tools/emu-lejos"
PLAT_RCX_SRC="src/platform/rcx"
PLAT_UNIX_SRC="src/platform/unix"
PLAT_GBOY_SRC="src/platform/gameboy"
IRTRCX_LIB_SRC="src/comms/libirtrcx"
JIRTRCX_LIB_SRC="src/comms/libjirtrcx"

export JAVA

default: emulator irtrcx_libs all_java scripts tinyvm_emul

all: default lejos_bin

release:
	$(MAKE) $(MFLAGS) clean
	rm -rf apidocs pcapidocs
	$(MAKE) $(MFLAGS) all
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

all_java:
	${ANT} all

scripts:
	chmod 775 $(LEJOS_HOME)/bin/lejosjc
	chmod 775 $(LEJOS_HOME)/bin/lejoslink
	chmod 775 $(LEJOS_HOME)/bin/lejosdl
	chmod 775 $(LEJOS_HOME)/bin/firmdl

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
	@echo ""
	@echo "====> Making leJOS RCX binary (lejos.srec)"
	@echo ""
	cd $(PLAT_RCX_SRC); $(MAKE) $(MFLAGS)

tinyvm_emul:
	@echo ""
	@echo "====> Making leJOS Unix binaries (lejos, for emulation)"
	@echo ""
	cd $(PLAT_UNIX_SRC); $(MAKE) $(MFLAGS)

javadoc:
	${ANT} doc

pcjavadoc:
	${ANT} pcdoc

visiondoc:
	${ANT} visiondoc

clean:
	${ANT} clean
	rm -f `find . -name '*.o'`
	rm -f `find . -name '*~'`
	rm -f `find . -name '*.core'`
	rm -f `find . -name '*.tvm'`
	rm -f `find . -name '*.bin'`
	rm -f `find . -name '*.sig'`
	rm -f `find . -name '*.bak'`
	rm -f `find . -name '*.stackdump'`
	rm -f `find . -name '*.backtrace'`
	rm -rf ./apidocs ./pcapidocs ./visionapidocs

distclean: clean
	rm -f `find . -name '.#*'`
	rm -f `find . -name '*.lst'`
	rm -f `find . -name '*.log'`

distclean_src: distclean
	rm -f bin/lejos bin/emu-dump bin/emu-lejos bin/emu-lejosrun
	rm -f src/java/tools/js/tinyvm/SpecialClassConstants.java src/java/tools/js/tinyvm/SpecialSignatureConstants.java src/javavm/specialclasses.h src/javavm/specialsignatures.h
	rm -f $(PLAT_UNIX_SRC)/dump_config $(PLAT_UNIX_SRC)/platform_config.h
	rm -f $(EMU_SRC)/mkimg
	rm -f `find . -name '*.so' -o -name '*.dylib' -o -name '*.jnilib' -o -name '*.dll'`
	rm -f `find . -name '.DS_Store'`	# Mac OS X Finder droppings
	rm -f `find . -name '*.exe'`

distclean_win: distclean
	/bin/strip `find . -name '*.exe'`

realclean: distclean_src
	rm -f `find bin -name '*.srec'`
