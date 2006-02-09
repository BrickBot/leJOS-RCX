#!/bin/bash

#
# compileDistributionCheck: utility to compile a test class for the lejOS distribution
#
# 09/02/06  created Matthias Paul Scholz


echo checking environment
cd ..

if [ "$LEJOS_HOME" = "$PWD" ]; then
    echo environment seems to be ok
    cd ./check
	echo compiling test class
	../bin/lejosjc DistributionSmokeTest.java
else
	echo ERROR: environment variable LEJOS_HOME is pointing to $LEJOS_HOME presently
  	echo however, it has to point to $PWD 
  	echo please set it accordingly.
  	cd ./check
fi
