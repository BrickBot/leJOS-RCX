/*
* $Log$
* Revision 1.2  2002/11/01 16:01:28  mpscholz
* changed startup display
*
* Revision 1.1  2002/09/28 10:29:34  mpscholz
* initial version for the test example of the remotecontrol package
*
*/

import josx.platform.rcx.*;
import josx.rcxcomm.remotecontrol.*;

/////////////////////////////////////////////////////////
/**
 *
 * This class is a main test program for the remote control sensor
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.0 (24/09/2002)
 */
public class RemoteControlTest implements RemoteControlListener {
    
    ////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////
    private static final char[] MESSAGE1 = {'M','S','G','1'};
    private static final char[] MESSAGE2 = {'M','S','G','2'};
    private static final char[] MESSAGE3 = {'M','S','G','3'};
    private static final char[] MOTOR_DOWN = {'M','O','T','-'};
    private static final char[] MOTOR_UP = {'M','O','T','+'};
    private static final char[] PROGRAM1 = {'P','R','O','G','1'};
    private static final char[] PROGRAM2 = {'P','R','O','G','2'};
    private static final char[] PROGRAM3 = {'P','R','O','G','3'};
    private static final char[] PROGRAM4 = {'P','R','O','G','4'};
    private static final char[] PROGRAM5 = {'P','R','O','G','5'};
    private static final char[] STOP = {'S','T','O','P'};
    private static final char[] SOUND = {'S','O','U','N','D'};
    private static final char[] READY = {'R','E','A','D','Y'};
    
    ////////////////////////////////////////////
    // fields
    ////////////////////////////////////////////
    /**
     * current sensor states (activated = true)
     */
    private boolean[] fSensorState = { false,false,false }; 
    
    ////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /** 
     * creates a new instance of RemoteControlTest 
     */
    public RemoteControlTest() {
        // reset to initial state
        reset();
        // display "ready"
        TextLCD.print(READY);		
    } // RemoteControlTest()
    
    ////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /**
     * main method for test reasons
     * @throws InterruptedException
     */
    public static void main(String[] args) 
        throws InterruptedException {
        // instantiate remote control test instance
        RemoteControlTest remoteControlTest = new RemoteControlTest();
        // create remote control sensor
        RemoteControlSensor sensor = new RemoteControlSensor();
        sensor.addRemoteControlListener(remoteControlTest);
        // reset engine
        remoteControlTest.reset();
        // just run until RUN button is pressed again
        Button.RUN.waitForPressAndRelease();
        System.exit( 0);
    } // main()

    ////////////////////////////////////////////
    // RemoteControListener interface methods
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    /**
    * handler for the message 1 button
    */
    public void message1Pressed() {
        // display
        TextLCD.print(MESSAGE1);
        // current sensor state?
        boolean sensorState = fSensorState[0];
        // activate/passivate sensor
        if(sensorState)
            Sensor.S1.passivate();
        else
            Sensor.S1.activate();
        // change sensor state
        fSensorState[0] = !sensorState;
    } // message1()

    ////////////////////////////////////////////
    /**
    * handler for the message 2 button
    */
    public void message2Pressed() {
        // display
        TextLCD.print(MESSAGE2);
        // current sensor state?
        boolean sensorState = fSensorState[1];
        // activate/passivate sensor
        if(sensorState)
            Sensor.S2.passivate();
        else
            Sensor.S2.activate();
        // change sensor state
        fSensorState[1] = !sensorState;
    } // message2()
    
    ////////////////////////////////////////////
    /**
    * handler for the message 3 button
    */
    public void message3Pressed() {
        // display
        TextLCD.print(MESSAGE3);
        // current sensor state?
        boolean sensorState = fSensorState[0];
        // activate/passivate sensor
        if(sensorState)
            Sensor.S3.passivate();
        else
            Sensor.S3.activate();
        // change sensor state
        fSensorState[2] = !sensorState;
    } // message3()

    ////////////////////////////////////////////
    /**
     * decrements motor power
     * @param aMotor the motor
     */
    public void motorDownPressed(Motor aMotor) {
        // display
        TextLCD.print(MOTOR_DOWN);
        // get current power
        int power = aMotor.getPower();
        // get current state
        if(power==0) {
            // move backward
            aMotor.setPower(++power);
            aMotor.backward();
        } else {
            if(aMotor.isForward())
                // decrease forward movement
                aMotor.setPower(--power);
            else {
                // move backward
                aMotor.setPower(Math.min(7,++power));
                aMotor.backward();
            } // else
        } // else
        // display power of motors
	displayMotorsPower();
    } // motorDownPressed()
	
    ////////////////////////////////////////////
    /**
     * increments motor power
     * @param aMotor the motor
     */
    public void motorUpPressed(Motor aMotor) {
        // display
        TextLCD.print(MOTOR_UP);
        // get current power
        int power = aMotor.getPower();
        // get current state
        if(power==0) {
            // move forward
            aMotor.setPower(++power);
            aMotor.forward();
        } else {
            if(aMotor.isBackward())
                // decrease backward movement
                aMotor.setPower(--power);
            else { 
                // move forward
                aMotor.setPower(Math.min(7,++power));
                aMotor.forward();
            } // else
        } // else
        // display power of motors
	displayMotorsPower();
    } // motorUpPressed()
	
    ////////////////////////////////////////////
    /**
    * handler for the program 1 button
    */
    public void program1Pressed() {
        // display
        TextLCD.print(PROGRAM1);
    } // program1()

    ////////////////////////////////////////////
    /**
    * handler for the program 2 button
    */
    public void program2Pressed() {
        // display
        TextLCD.print(PROGRAM2);
    } // program2()

    ////////////////////////////////////////////
    /**
    * handler for the program 3 button
    */
    public void program3Pressed() {
        // display
        TextLCD.print(PROGRAM3);
    } // program3()

    ////////////////////////////////////////////
    /**
    * handler for the program 4 button
    */
    public void program4Pressed() {
        // display
        TextLCD.print(PROGRAM4);
    } // program4()

    ////////////////////////////////////////////
    /**
    * handler for the program 5 button
    */
    public void program5Pressed() {
        // display
        TextLCD.print(PROGRAM5);
    } // program5()

    ////////////////////////////////////////////
    /**
    * handler for the sound button
    */
    public void soundPressed() {
        // display
        TextLCD.print(SOUND);
        // sound
        Sound.beep();
    } // sound()

    ////////////////////////////////////////////
    /**
    * handler for the stop button
    */
    public void stopPressed() {
        // display
        TextLCD.print(STOP);
        // reset
        reset();
    } // stop()

    ////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /**
     * resets the engine to its initial state
     */
    public void reset() {
        // stop motors
        Motor.A.stop();	
        Motor.A.setPower(0);	
        Motor.B.stop();
        Motor.B.setPower(0);	
        Motor.C.stop();
        Motor.C.setPower(0);	
        // passivate sensors
        Sensor.S1.passivate();
        Sensor.S2.passivate();
        Sensor.S3.passivate();
        for(int i=0;i<fSensorState.length;i++)
            fSensorState[i] = false;
    } // reset()

    ////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    /**
    * displays the power of the motors.
    * <br>format: 0<Motor A><Motor B><Motor C>
    */	
    private void displayMotorsPower() {
        int powers = Motor.A.getPower() * 100
            + Motor.B.getPower() * 10
            + Motor.C.getPower(); 
        LCD.showNumber(powers);
    } // displayMotorsPower()
    
} // class RemoteControlTest
