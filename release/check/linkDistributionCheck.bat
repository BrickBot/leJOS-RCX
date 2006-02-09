@echo off

rem
rem linkDistributionCheck: utility to link a test class for the lejOS distribution
rem
rem 09/02/06  created Matthias Paul Scholz


echo linking test class
..\bin\lejoslink.bat DistributionSmokeTest -o DistributionSmokeTest.bin

