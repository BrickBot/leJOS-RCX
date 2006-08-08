#!/bin/bash

#
# checkDistribution: script to check the lejOS distribution by compiling and downloading a test class to the RCX
#
# 08/08/06  created Matthias Paul Scholz

echo checking environment
cd ..

if [ "$LEJOS_HOME" = "$PWD" ]; then
    echo environment seems to be ok
    cd ./check
	echo compiling test class
	lejosjc DistributionSmokeTest.java
	echo linking and downloading test class
	lejos -v DistributionSmokeTest
	echo done. If no error occurred, please press the RUN Button on your RCX now. The LCD of the RCX should display SMOKE, then TEST
else
	echo ERROR: environment variable LEJOS_HOME is pointing to $LEJOS_HOME presently
  	echo however, it has to point to $PWD 
  	echo please set it accordingly.
  	cd ./check
fi

