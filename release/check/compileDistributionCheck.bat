@echo off

rem
rem compileDistributionCheck: utility to compile a test class for the lejOS distribution
rem
rem 09/02/06  created Matthias Paul Scholz


echo checking environment
cd ..

if not "%LEJOS_HOME%" == "%PWD%" goto lejos_home_set

:lejos_home_not_set  
	echo ERROR: environment variable LEJOS_HOME is pointing to %LEJOS_HOME% presently
  	echo however, it has to point to %PWD%. 
  	echo please set it accordingly.
  	cd .\check
	goto end

:lejos_home_set  

    echo environment seems to be ok
    cd .\check
	echo compiling test class
	..\bin\lejosjc.bat DistributionSmokeTest.java

: end
