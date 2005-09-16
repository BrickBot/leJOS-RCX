/*
* $Log$
* Revision 1.1  2003/05/09 21:44:00  mpscholz
* moved remote control classes to josx.rcxcomm
*
* Revision 1.1  2002/09/28 10:32:27  mpscholz
* initial version of the remotecontrol package
*
*/

package josx.rcxcomm.remotecontrol;

import josx.platform.rcx.Motor;

/////////////////////////////////////////////////////////
/**
 *
 * This class is an adapter for the remote control listener.
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.0 (24/09/2002)
 */
public class RemoteControlAdapter implements RemoteControlListener {
    
    ////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    // fields
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /** 
     * creates a new instance of RemoteControlAdapter 
     */
    public RemoteControlAdapter() {
    } // RemoteControlAdapter()
    
    ////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    // RemoteControListener interface methods
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    /**
    * handler for the message 1 button
    */
    public void message1Pressed() {
        // no-op
    } // message1()

    ////////////////////////////////////////////
    /**
    * handler for the message 2 button
    */
    public void message2Pressed() {
        // no-op
    } // message2()
    
    ////////////////////////////////////////////
    /**
    * handler for the message 3 button
    */
    public void message3Pressed() {
        // no-op
    } // message3()

    ////////////////////////////////////////////
    /**
     * decrement motor power
     * @param aMotor the motor
     */
    public void motorDownPressed(Motor aMotor) {
        // no-op
    } // motorDownPressed()
	
    ////////////////////////////////////////////
    /**
     * increment motor power
     * @param aMotor the motor
     */
    public void motorUpPressed(Motor aMotor) {
        // no-op
    } // motorUpPressed()
	
    ////////////////////////////////////////////
    /**
    * handler for the program 1 button
    */
    public void program1Pressed() {
        // no-op
    } // program1()

    ////////////////////////////////////////////
    /**
    * handler for the program 2 button
    */
    public void program2Pressed() {
        // no-op
    } // program2()

    ////////////////////////////////////////////
    /**
    * handler for the program 3 button
    */
    public void program3Pressed() {
        // no-op
    } // program3()

    ////////////////////////////////////////////
    /**
    * handler for the program 4 button
    */
    public void program4Pressed() {
        // no-op
    } // program4()

    ////////////////////////////////////////////
    /**
    * handler for the program 5 button
    */
    public void program5Pressed() {
        // no-op
    } // program5()

    ////////////////////////////////////////////
    /**
    * handler for the sound button
    */
    public void soundPressed() {
        // no-op
    } // sound()

    ////////////////////////////////////////////
    /**
    * handler for the stop button
    */
    public void stopPressed() {
        // no-op
    } // stop()

    ////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////
    
} // class RemoteControlAdapter
