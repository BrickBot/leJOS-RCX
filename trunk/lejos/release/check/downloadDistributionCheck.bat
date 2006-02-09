@echo off

rem
rem downloadDistributionCheck: utility to download a test class for lejOS distribution to the RCX
rem
rem 09/02/06  created Matthias Paul Scholz

echo downloading test class
..\bin\lejosdl.bat DistributionSmokeTest.bin
echo done. 
echo if no error occurred, please press the RUN Button on your RCX now
echo the RCX's LCD should display SMOKE, then TEST

