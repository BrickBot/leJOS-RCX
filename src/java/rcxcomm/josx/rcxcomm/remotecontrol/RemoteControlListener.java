/*
* $Log$
* Revision 1.1  2003/05/09 21:44:00  mpscholz
* moved remote control classes to josx.rcxcomm
*
* Revision 1.1  2002/09/28 10:32:28  mpscholz
* initial version of the remotecontrol package
*
*/

package josx.rcxcomm.remotecontrol;

import josx.platform.rcx.Motor;

/////////////////////////////////////////////////////////
/**
 *
 * This is an interface for remote control message handlers.
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.0 (24/09/2002)
 */
public interface RemoteControlListener {
    
    ////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    // fields
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /**
    * handler for the message 1 button
    */
    public void message1Pressed();

    ////////////////////////////////////////////
    /**
    * handler for the message 2 button
    */
    public void message2Pressed();

    ////////////////////////////////////////////
    /**
    * handler for the message 3 button
    */
    public void message3Pressed();

    ////////////////////////////////////////////
    /**
    * handler for the program 1 button
    */
    public void program1Pressed();

    ////////////////////////////////////////////
    /**
    * handler for the program 2 button
    */
    public void program2Pressed();

    ////////////////////////////////////////////
    /**
    * handler for the program 3 button
    */
    public void program3Pressed();
    
    ////////////////////////////////////////////
    /**
    * handler for the program 4 button
    */
    public void program4Pressed();

    ////////////////////////////////////////////
    /**
    * handler for the program 5 button
    */
    public void program5Pressed();
    
    ////////////////////////////////////////////
    /**
    * handler for the motor down button
     *@param aMotor the motor
    */
    public void motorDownPressed(Motor aMotor);

    ////////////////////////////////////////////
    /**
    * handler for the motor up button
     *@param aMotor the motor
    */
    public void motorUpPressed(Motor aMotor);
	
    ////////////////////////////////////////////
    /**
    * handler for the sound button
    */
    public void soundPressed();

    ////////////////////////////////////////////
    /**
    * handler for the stopp button
    */
    public void stopPressed();
    
} // interface RemoteControlListener
