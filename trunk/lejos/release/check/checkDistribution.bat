echo off
rem checkDistribution: script to check the lejOS distribution by compiling and downloading a test class to the RCX
rem 08/08/06  created Matthias Paul Scholz

if "%LEJOS_HOME%" == ""  goto homeless

echo compiling test class
call lejosjc.bat DistributionSmokeTest.java
echo linking and downloading test class
call lejos.bat -v DistributionSmokeTest
echo done. If no error occurred, please press the RUN Button on your RCX now. The LCD of the RCX should display SMOKE, then TEST

:homeless
echo LEJOS_HOME not defined

:end


