echo off
if "$LEJOS_HOME" == "" goto end
javac -target 1.1 -bootclasspath %LEJOS_HOME%\lib\classes.jar;%LEJOS_HOME%\lib\rcxrcxcomm.jar;%CLASSPATH% %1 %2 %3 %4 %5 %6 %7 %8 %9
:end

