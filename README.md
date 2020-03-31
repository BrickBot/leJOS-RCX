# leJOS RCX  ![Java CI](https://github.com/BrickBot/leJOS-RCX/workflows/Java%20CI/badge.svg)
A tiny Java Virtual Machine for the Lego MindStorms RCX, containing a VM for Java bytecodes and additional software to load and run Java programs.

## Notes
* The source code as available in the leJOS RCX repository on SourceForge does not successfully finish a complete build; this project attempts to remediate that
* The librcx float code has been updated based on the latest patches in brickOS
* There is an unfortunate mixing and matching of ant and make; as such some configuration settings must be made in multiple places
* Configuration settings
  - Build configurations are set to support Debian/Ubuntu to provide support for the greatest number of use cases
    + Linux, both 32-bit and 64-bit
    + Windows, via both Windows Subsystem for Linux and as a Hyper-V "Quick Create" option
  - The "vision.jar" target is disabled for now due to dependencies on deprecated package com.sun.image.codec.jpeg
  - Floating point arithmetic is currently disabled due to an internal error thrown by the h8300-hms-gcc cross-compiler
  - In build.properties
    + Change the "lejos.ostype" property according to your particular platform type
    + Firmware building, which requires the h8/300 cross-toolchain is currently configured to be built by default; comment out the property "build.lejos.firmware" to skip this


## Known Issues
1. Floating Point Disabled:  Internal error thrown by the version of the h8/300 cross-toolchain available on Debian/Ubuntu
```
void do_fcmp (JFLOAT f1, JFLOAT f2, STACKWORD def)
{
  if (f1 > f2)
    push_word (1);
  else if (f1 == f2)
    push_word (0);
  else if (f1 < f2)
    push_word (-1);
  else 
    push_word (def);
}

src/javavm/Makefile.include:3: recipe for target 'interpreter.o' failed
src/javavm/interpreter.c: In function `engine':
src/javavm/interpreter.c:78: internal compiler error: in byte_reg, at config/h8300/h8300.c:342
Please submit a full bug report,
with preprocessed source if appropriate.
See <URL:http://gcc.gnu.org/bugs.html> for instructions.
make: *** [interpreter.o] Error 1

BUILD FAILED
```

2. Problems generating firmdl2x.srec and firmdl4x.srec:  Without changing any files, a build might succeed, or it might fail
```
From src/comms/tools/fastdl/Makefile
	# Every once in a while, CodePacker doesn't run correctly,
	#  and the generated rcx.lds file is missing the added (.text) section
	#  between the __extra_start and __extra_end lines.
	#  The "packing size" value output by CodePacker is also usually 0 in these cases.
	#  A clean and rebuild, with no other changes, usually succeeds.
```


## IDE Configurations
* Eclipse:  Plugins available at the [leJOS-RCX-Eclipse](https://github.com/BrickBot/leJOS-RCX-Eclipse) project site
* Netbeans: [Archived _Makezine_ article](https://web.archive.org/web/20100117085123/http://www.makezine.com/extras/64.html)

## Alternate API
* [leJOS-RCX-Alt-API](https://github.com/BrickBot/leJOS-RCX-Alt-API):  An alternative to part of the standard leJOS API designed to fit an objects-early approach to teaching programming
  - Perhaps could be incorporated into the mainstream leJOS-RCX release at some point?

## Utilities
* [leJOS-RCX-MindStormsTools](https://github.com/BrickBot/leJOS-RCX-MindStormsTools)
  - A small toolkit to aid in the uploading of Java programs to LEGO MindStorms RCX bricks, providing functionality of the leJOS library in the development environment for this purpose
* [leJOS-RCX-Tools](https://github.com/BrickBot/leJOS-RCX-Tools)
  - A visual interface for leJOS. RCXDownload automatically sets the JDK-, leJOS- and ClassPaths, compiles the chosen Java-Source, shows the compiler messages and is able to link and load both the compiled classes and the leJOS-firmware.
* [leJOS-RCX-TextLCDApplet](https://github.com/BrickBot/leJOS-RCX-TextLCDApplet)
  - Java Applet GUI for testing josx.platform.rcx.TextLCD
