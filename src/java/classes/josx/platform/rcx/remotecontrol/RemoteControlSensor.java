/*
 * $Log$
 */

package josx.platform.rcx.remotecontrol;

import java.io.*;
import java.util.*;
import josx.platform.rcx.*;

/////////////////////////////////////////////////////////
/**
 *
 * This class is a sensor for remote control messages.
 * <br>It listens for remote control messages and triggers the registered listener
 * methods.
 * <br>The sensor uses the built-in lejos SerialListener thread
 * <p>Remote control opcodes where supplied by C. Ponsard in lejos 2.0 remotectrl example.
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.0 (24/09/2002)
 */
public class RemoteControlSensor implements SerialListener,Opcode {
    
    ////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    // fields
    ////////////////////////////////////////////
    /**
     * the remote control listeners
     */
    private Vector fListeners = null;
    
    ////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /** 
     * creates a new instance of RemoteControlSensor 
     */
    public RemoteControlSensor() {
        // instantiate listeners vector
        fListeners = new Vector(2,2);
        // add as SerialListener to Serial 
        Serial.addSerialListener(this);
    } // RemoteControl()
    
    ////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /**
      adds a remote control listener
      @param aListener a remote control listener
      @see RemoteControlListener 
    */
    public void addRemoteControlListener(RemoteControlListener aListener) {
        fListeners.addElement(aListener);
    } // addRemoteControlListener()
    
    ////////////////////////////////////////////
    /**
     * removes a remote control listener
     * @param aListener a remote control listener
     * @see RemoteControlListener 
    */
    public void removeRemoteControlListener(RemoteControlListener aListener) {
        fListeners.removeElement(aListener);
    } // removeRemoteControlListener()

    ////////////////////////////////////////////
    /**
     * implements the SerialListener interface
     * <br>listens for incoming packets on the IR port
     * and notifies the registered listeners.
     * @param aPacket the packet data received
     * @param aLength the length of the packet
    */
    public void packetAvailable(byte[] aPacket, int aLength) {
        // message to rcx? 
        // first byte of remote command messages to the rcx is always 
        // OPCODE_REMOTE_COMMAND
        if((aPacket[0]&255)!=(OPCODE_REMOTE_COMMAND&255)) 
            return;
        // valid paket?
        if(aLength<3) 
            return;
        // inspect packet opcode
        int opcode = (aPacket[1]&255)*100+(aPacket[2]&255);
        inspect(opcode);
    } // packetAvailable()

    ////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    /**
     * inspects the opcode sent.
     * <br>opcode values provided by C. Ponsard in lejos 2.0 remotectrl example
     * @param anOpcode the opcode 
     */
    private void inspect(int anOpcode) {
        // check codes & trigger assigned handler
        // a bit wordy due to the lacking of the switch-statement
        // "message 1" pressed
        if (anOpcode==1) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).message1Pressed();
        // "message 2" pressed
        else if (anOpcode==2) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).message2Pressed();
        // "message 3" pressed
        else if (anOpcode==4) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).message3Pressed();
        // "motor a +" pressed
        else if(anOpcode==8) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorUpPressed(
                    Motor.A);
        // "motor b +" pressed
        else if(anOpcode==16) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorUpPressed(
                    Motor.B);
        // "motor c +" pressed
        else if(anOpcode==32) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorUpPressed(
                    Motor.C);
        // "motor a -" pressed
        else if (anOpcode==64) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorDownPressed(
                    Motor.A);
        // "motor b -" pressed
        else if(anOpcode==128) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorDownPressed(
                    Motor.B);
        // "motor c -" pressed
        else if (anOpcode==100) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorDownPressed(
                    Motor.C);
        // program buttons
        else if(anOpcode==200) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program1Pressed();
        else if(anOpcode==400) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program2Pressed();
        else if(anOpcode==800) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program3Pressed();
        else if(anOpcode==1600) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program4Pressed();
        else if(anOpcode==3200) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program5Pressed();
        // "stop" pressed
        else if(anOpcode==6400) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).stopPressed();
        // "sound" pressed
        else if(anOpcode==12800) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).soundPressed();
        // wait
        try{
            Thread.sleep(50); 
        } catch(InterruptedException intExc) {
            // no-op
        } // catch()
    } // // inspect()
    
    
 

} // class RemoteControlSensor
