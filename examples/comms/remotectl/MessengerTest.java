/*
 * $Log$
 * Revision 1.2  2004/05/01 13:52:08  starblue
 * update imported package name
 *
 * Revision 1.1  2003/05/01 11:58:07  mpscholz
 * an example for using the RemoteControlMessenger
 *
 */

import java.io.*;
import josx.rcxcomm.remotecontrol.*;

/////////////////////////////////////////////////////////
/**
 *
 * This class is a sample test for josx.platform.rcx.remotecontrol.Messenger 
 * <br>It sends some remote control messages to the RCX
 *
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.0 (01/05/2003)
 */
public class MessengerTest {
    
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
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /**
     * main method for test reasons
     * argument: port ("USB", "COM1" etc)
     */
    public static void main(String[] args) { 
        try {
	    	if(args.length!=0) {
	    		throw new Exception("first argument must be the tower port (USB, COM1, etc)");
	    	}
	        // instantiate messenger instance
	        RemoteControlMessenger remoteControlMessenger = new RemoteControlMessenger(args[0]);
            // send some commands
            // start program 1
            System.out.println("starting program 1");
            remoteControlMessenger.send(RemoteControlMessenger.PROGRAM1);
            // beep
            System.out.println("beeping");
            remoteControlMessenger.send(RemoteControlMessenger.SOUND);
            // wait two seconds
            try {
                Thread.sleep(2000);
            } catch(InterruptedException irexc) {
                // do nothing
            } // catch
            // motor A up
            System.out.println("starting motor A");
            remoteControlMessenger.send(RemoteControlMessenger.MOTOR_A_UP);
            // wait two seconds
            try {
                Thread.sleep(2000);
            } catch(InterruptedException irexc) {
                // do nothing
            } // catch
            // stop program 
            System.out.println("stopping program");
            remoteControlMessenger.send(RemoteControlMessenger.STOP);
            // accomplished
            System.out.println("accomplished");
        } catch(Exception exc) {
            // something went wrong
            System.out.println("an error occurred: " + exc.getMessage());
            exc.printStackTrace();
        } // catch
    } // main()

    ////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////
    
} // class MessengerTest
