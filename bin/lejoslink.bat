@echo off
set CLASSPATH_SAVE=%CLASSPATH%
if "%LEJOS_HOME%" == ""  goto homeless

set CLASSPATH=%LEJOS_HOME%\lib\jtools.jar;%CLASSPATH%
java -Dtinyvm.linker=lejoslink -Dtinyvm.home=%LEJOS_HOME% -Dtinyvm.write.order=BE js.tinyvm.TinyVM -classpath %LEJOS_HOME%\lib\classes.jar;%LEJOS_HOME%\lib\rcxrcxcomm.jar;%CLASSPATH% %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:homeless
echo LEJOS_HOME not defined

:end
set CLASSPATH=%CLASSPATH_SAVE%

