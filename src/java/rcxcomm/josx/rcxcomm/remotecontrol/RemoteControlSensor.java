/*
 * $Log$
 * Revision 1.2  2005/09/16 16:37:10  markus_heiden
 * mh: organized imports
 *
 * Revision 1.1  2003/05/09 21:44:00  mpscholz
 * moved remote control classes to josx.rcxcomm
 *
 * Revision 1.2  2002/11/01 16:03:35  mpscholz
 * support of LEGO F7 protocol
 *
 * Revision 1.1  2002/09/28 10:32:28  mpscholz
 * initial version of the remotecontrol package
 *
 */

package josx.rcxcomm.remotecontrol;

import java.util.Vector;

import josx.platform.rcx.Motor;
import josx.platform.rcx.Opcode;
import josx.platform.rcx.Serial;
import josx.platform.rcx.SerialListener;
import josx.platform.rcx.Sound;

/////////////////////////////////////////////////////////
/**
 *
 * This class is a sensor for remote control messages.
 * <br>It listens for remote control messages and triggers the registered listener
 * methods.
 * <br>F7 LEGO firmware opcodes are supported also which means that the sender could use RCXF7Port
 * <br>instead of the LEGO remote control
 * <br>The sensor uses the built-in lejos SerialListener thread
 *
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.1 (16/10/2002)
 */
public class RemoteControlSensor implements SerialListener,Opcode {
    
    ////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////
    /**
     * the F7 LEGO firmware F7 opcode byte
     */
    private static final byte F7_OPCODE = (byte)0xF7;
    
    ////////////////////////////////////////////
    // fields
    ////////////////////////////////////////////
    /**
     * the F7 LEGO firmware F7 acknowledge packet
     */
    private byte [] fF7AckPacket = { F7_OPCODE,0x00 };
    /**
     * the F7 buffer
     */
    private byte[] fF7Buffer = { 0x00,0x00 };
    /**
     * the F7 packet counter
     */
    private int fF7Counter = 0;
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
        // add a SerialListener to Serial 
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
        // length must be 3 at least
        if(aLength<3) 
            return;
        // opcode
        int opcode = 0;
        // check protocol
        byte protocol = aPacket[0];
        // we support F7 LEGO firmware protocol also
        if(protocol==F7_OPCODE) {
            // put first opcode byte into F7 buffer
            fF7Buffer[fF7Counter] = aPacket[1];
            // send an ack
            if(!acknowledgeF7(aPacket)) {
                // acknowledgement failed
                Sound.buzz();
                return;
            } // if
            // increment F7 counter
            fF7Counter++;
            // complete F7 message?
            if(fF7Counter<2) {
                // wait for next F7 byte
                return;
            } else {
                // reset F7 counter
                fF7Counter = 0;
                // inspect F7 message
                opcode = (fF7Buffer[0]&255)*256+(fF7Buffer[1]&255);
            } // if
        } // if
        // if no F7, we check for the standard LEGO remote control protocol 
        else if((protocol&255)==(OPCODE_REMOTE_COMMAND&255)) {
            opcode = (aPacket[1]&255)*256+(aPacket[2]&255);
        } else
            return;
        // now inspect packet opcode
        inspect(opcode);
    } // packetAvailable()

    ////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    /**
     * sends acknowledge data for F7 LEGO firmware messages
     * @param byte[] the incoming packet to acknowlegde  
     * @return true if the acknowledge data was successfully sent, else false
     */
    private boolean acknowledgeF7(byte[] anIncomingPacket) {
        synchronized (this) {
            // build acknowledge data
            byte incoming = anIncomingPacket[1];
            fF7AckPacket[1] = (byte) ~incoming;
            // wait if Serial is sending at the moment
            while(Serial.isSending()) 
                Thread.yield();
            // send data
            return Serial.sendPacket(fF7AckPacket,0,2);
        } // synchronized
    } // acknowledge()

    ////////////////////////////////////////////
    /**
     * inspects the opcode sent.
     * @param anOpcode the opcode 
     */
    private void inspect(int anOpcode) {
        // check codes & trigger assigned handler
        // a bit wordy due to the lacking of the switch-statement
        // "message 1" pressed
        if (anOpcode==0x01)  
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).message1Pressed();
        // "message 2" pressed
        else if (anOpcode==0x02) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).message2Pressed();
        // "message 3" pressed
        else if (anOpcode==0x04) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).message3Pressed();
        // "motor a +" pressed
        else if(anOpcode==0x08) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorUpPressed(
                    Motor.A);
        // "motor b +" pressed
        else if(anOpcode==0x10) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorUpPressed(
                    Motor.B);
        // "motor c +" pressed
        else if(anOpcode==0x20) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorUpPressed(
                    Motor.C);
        // "motor a -" pressed
        else if (anOpcode==0x40) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorDownPressed(
                    Motor.A);
        // "motor b -" pressed
        else if(anOpcode==0x80) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorDownPressed(
                    Motor.B);
        // "motor c -" pressed
        else if (anOpcode==0x100) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).motorDownPressed(
                    Motor.C);
        // program buttons
        else if(anOpcode==0x200) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program1Pressed();
        else if(anOpcode==0x400) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program2Pressed();
        else if(anOpcode==0x800) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program3Pressed();
        else if(anOpcode==0x1000) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program4Pressed();
        else if(anOpcode==0x2000) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).program5Pressed();
        // "stop" pressed
        else if(anOpcode==0x4000) 
            // notify listeners
            for(int i=0;i<fListeners.size();i++)
                ((RemoteControlListener)fListeners.elementAt(i)).stopPressed();
        // "sound" pressed
        else if(anOpcode==0x8000) 
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
