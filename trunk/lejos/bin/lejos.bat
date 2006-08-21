@echo off

rem lejos: Java tool to link and download java programs for LeJOS
rem 08/08/06  created Matthias Paul Scholz

if "%LEJOS_HOME%" == ""  goto homeless

set THIRDPARTY_LIBS="%LEJOS_HOME%\3rdparty\lib"
set LINK_CLASSPATH=.;%LEJOS_HOME%\lib\classes.jar;%LEJOS_HOME%\lib\jtools.jar;%THIRDPARTY_LIBS%\commons-cli-1.0.jar;%THIRDPARTY_LIBS%\bcel-5.1.jar;%LEJOS_HOME%\lib\rcxcomm.jar;%LEJOS_HOME%\lib\pcrcxcomm.jar

java -Djava.library.path="%LEJOS_HOME%\bin" -classpath "%LINK_CLASSPATH%" js.tools.LejosLinkAndDownload --writeorder BE --classpath "%LINK_CLASSPATH%" -tty %RCXTTY% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto end

:homeless
echo LEJOS_HOME not defined

:end
