/*
 * $Log$
 */

import java.io.*;
import josx.platform.rcx.remotecontrol.*;

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
     */
    public static void main(String[] args) { 
        // instantiate messenger instance
        RemoteControlMessenger remoteControlMessenger = new RemoteControlMessenger();
        try {
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
        } catch(IOException exc) {
            // something went wrong
            System.out.println("an error occurred: " + exc.getMessage());
            exc.printStackTrace();
        } // catch
    } // main()

    ////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////
    
} // class MessengerTest
