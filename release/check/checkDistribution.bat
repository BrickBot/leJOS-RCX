@echo off

rem
rem checkDistribution: utility to check the lejOS distribution after having downloaded it
rem
rem 02/12/05  created Matthias Paul Scholz


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
	echo linking test class
	..\bin\lejoslink.bat DistributionSmokeTest -o DistributionSmokeTest.bin
	echo downloading test class
	..\bin\lejosdl.bat DistributionSmokeTest.bin
	echo done. 
	echo if no error occurred, please press the RUN Button on your RCX now...

: end
