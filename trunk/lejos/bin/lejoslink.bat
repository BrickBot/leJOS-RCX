@echo off
if "%LEJOS_HOME%" == ""  goto homeless

java -classpath %LEJOS_HOME%\lib\classes.jar;%LEJOS_HOME%\lib\rcxcomm.jar;%LEJOS_HOME%\lib\commons-cli-1.0.jar;%LEJOS_HOME%\lib\bcel-5.1.jar;%LEJOS_HOME%\lib\jtools.jar;%CLASSPATH% js.tinyvm.TinyVM --writeorder BE %1 %2 %3 %4 %5 %6 %7 %8 %9 
goto end

:homeless
echo LEJOS_HOME not defined

:end
