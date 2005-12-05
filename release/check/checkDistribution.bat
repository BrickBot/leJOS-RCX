#!/bin/bash

#
# checkDistribution: utility to check the lejOS distribution after having downloaded it
#
# 02/12/05  created Matthias Paul Scholz

if "%LEJOS_HOME%" != "" goto lejos_home_set

:lejos_home_not_set  
	REM ERROR: environment variable LEJOS_HOME is not set!
	REM Please export it, pointing to the directory you installed this distribution into.
	goto end

:lejos_home_set  

	REM compiling test class
	..\bin\lejosjc DistributionSmokeTest.java
	REM linking test class
	..\bin\lejoslink DistributionSmokeTest -o DistributionSmokeTest.bin
	REM downloading test class
	..\bin\lejosdl DistributionSmokeTest.bin
	REM Done. Now press the RUN Button on your RCX...

: end
