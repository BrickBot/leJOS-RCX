Speech control example
----------------------

This examples need IBM's ViaVoice installed on your PC. ViaVoice is commercial software.

The example program uses the lejos Vision RCX Remote Control class to send 
remote control commands to the RCX. 
        
It uses the IBM implementation of the Java Speech API and IBM ViaVoice for 
voice recognition.
 
As well as IBM ViaVoice, it needs a copy of the IBM alphaWorks 
Speech for Java software downloaded from http://www.alphaworks.ibm.com/tech/speech 
and installed. 

It needs the Rover.java program in the rcx subdirectory running on the RCX. This
should be complied with lejosc and downloaded to the RCX using the lejos command.

Compile SpeechControl.java with javac, and run it by "java SpeechControl". 

Make sure that the IBM alphaWorks Java Speech jar (imbjs.jar), the lejos vision api jar 
(vision.jar) and the josx.rcxcomm package (pcrcxcomm.jar) are on the classpath.

You also need the alphaworks Java Speech API dlls (in ibmjs/lib) on the PATH.

Run "java SpeechControl" and you can control the robot using the commands 
in rover_en.gram, such as "forwards", "backwards", "left", "right", 
"up" (tilt camera up), "down". "stop" etc. 

Say "Goodbye" to stop the program.

SpeechControl.java is a modified version of IBM's hello test program.
