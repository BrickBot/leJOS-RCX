#!/bin/bash

#
# checkDistribution: utility to check the lejOS distribution after having downloaded it
#
# 02/12/05  created Matthias Paul Scholz

echo checking environment
cd ..

if "%LEJOS_HOME%" != "%PWD%" goto lejos_home_set

:lejos_home_not_set  
	REM ERROR: environment variable LEJOS_HOME is pointing to %LEJOS_HOME% presently
  	REM however, it has to point to %PWD%. 
  	REM please set it accordingly.
  	cd .\check
	goto end

:lejos_home_set  

    REM environment seems to be ok
    cd .\check
	REM compiling test class
	..\bin\lejosjc DistributionSmokeTest.java
	REM linking test class
	..\bin\lejoslink DistributionSmokeTest -o DistributionSmokeTest.bin
	REM downloading test class
	..\bin\lejosdl DistributionSmokeTest.bin
	REM done. 
	REM if no error occurred, please press the RUN Button on your RCX now...

: end
