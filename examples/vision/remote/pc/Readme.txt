Remote Control Example

This examples uses the Rover RCX program used by several examples. 

Rover requires a wheeled robot, that can spin on the spot, and which has the left wheels driven 
by Motor A, and the right wheels by Motor C. It requires a camera and should use Motor B 
to tilt the camera up and down. It also needs sensor 1 connected to a single sensor bumper.

Rover responds to remote controls, and moves back and stops if its bumper hits an obstacle.

Rover.java is in the "rcx" subdirectory. Compile it with lejos.

The RemoteControl program in the "pc" directory adds to the Vision image viewer some GUI controls
to provide a remote control console. The console allows the the robot to move forwards and backwards, 
spin left and right and stop. It also allows the camera to be tilted up and down.

In addition RemoteControl makes the Rover follow red objects. You can manoeuvre the robot to a red object
and it will push it about. Three regions are defined for detecting objects. Color display windows show the
color being looked for in each region, and the average color value currently detected in the region. When a red
object is detected in the left region the robot moves right and if in the right region, it moves left. If a
red object is detected in the center region, the robot moves forward and plays a sound. If you lose track of 
the object you can use the remote controls to find it again.

The effect controls (obtainable by clicking on the little button on the bottom right of the image viewer) can
be used to change the sensitivity of the color detection - both the tolerance on individiual pixels and the
proportion of detected pixels in the region.

RemoteControl should be compiled with "javac" and executed with "java". It has only been tested with JDK 1,3 and
1.4. It needs vision.jar and pcrcxcomm.jar on the CLASSPATH.
