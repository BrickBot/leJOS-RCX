@echo off
if "%LEJOS_HOME%" == ""  goto homeless
if "%RCX_PORT%" == ""  goto rcx_portless

set THIRDPARTY_LIBS="%LEJOS_HOME%\3rdparty\lib"


java -Djava.library.path="%LEJOS_HOME%\bin" -classpath "%LEJOS_HOME%\lib\jtools.jar;%THIRDPARTY_LIBS%\commons-cli-1.0.jar;%LEJOS_HOME%\lib\pcrcxcomm.jar" js.tools.Firmdl --tty %RCX_PORT% %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:homeless
echo LEJOS_HOME not defined
goto end

:rcx_portless
echo RCX_PORT not defined

:end
