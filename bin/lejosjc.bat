@echo off
if "%LEJOS_HOME%" == ""  goto homeless

javac -bootclasspath %LEJOS_HOME%\lib\classes.jar;%LEJOS_HOME%\lib\rcxrcxcomm.jar;%CLASSPATH% %1 %2 %3 %4 %5 %6 %7 %8 %9
rem javac -target 1.1 -bootclasspath %LEJOS_HOME%\lib\classes.jar;%LEJOS_HOME%\lib\rcxrcxcomm.jar;%CLASSPATH% %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:homeless
echo LEJOS_HOME not defined

:end
