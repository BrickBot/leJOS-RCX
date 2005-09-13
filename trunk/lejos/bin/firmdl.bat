@echo off
if "%LEJOS_HOME%" == ""  goto homeless

java -classpath %LEJOS_HOME%\lib\jtools.jar;%LEJOS_HOME%\lib\commons-cli-1.0.jar;%LEJOS_HOME%\lib\pcrcxcomm.jar js.tools.Firmdl %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:homeless
echo LEJOS_HOME not defined

:end
