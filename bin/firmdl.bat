echo off
if "$LEJOS_HOME" == "" goto end
java -classpath %LEJOS_HOME%\lib\jtools.jar;%LEJOS_HOME%\lib\pcrcxcomm.jar js.tools.Firmdl %1 %2 %3 %4 %5 %6 %7 %8 %9
:end

