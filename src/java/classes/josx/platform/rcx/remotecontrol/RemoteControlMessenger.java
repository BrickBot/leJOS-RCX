package josx.platform.rcx.remotecontrol;
/*
 * $Log$
 */
import josx.rcxcomm.*;
import java.io.*;

/////////////////////////////////////////////////////////
/**
 *
 * This class is a Messenger for remote control messages.
 * <br>It sends remote control messages; 
 * the original LEGO remote control opcodes are used
 *
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.0 (01/05/2003)
 */
public class RemoteControlMessenger {
    
    ////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////
    public static final byte[] NONE = { (byte)0x00,(byte)0x00 };
    public static final byte[] MESSAGE1 = { (byte)0x00,(byte)0x01 };
    public static final byte[] MESSAGE2 = { (byte)0x00,(byte)0x02 };
    public static final byte[] MESSAGE3 = { (byte)0x00,(byte)0x04 };
    public static final byte[] MOTOR_A_UP = { (byte)0x00,(byte)0x08 };
    public static final byte[] MOTOR_B_UP = { (byte)0x00,(byte)0x10 };
    public static final byte[] MOTOR_C_UP = { (byte)0x00,(byte)0x20 };
    public static final byte[] MOTOR_A_DOWN = { (byte)0x00,(byte)0x40 };
    public static final byte[] MOTOR_B_DOWN = { (byte)0x00,(byte)0x80 };
    public static final byte[] MOTOR_C_DOWN = { (byte)0x01,(byte)0x00 };
    public static final byte[] PROGRAM1 = { (byte)0x02,(byte)0x00 };
    public static final byte[] PROGRAM2 = { (byte)0x04,(byte)0x00 };
    public static final byte[] PROGRAM3 = { (byte)0x08,(byte)0x00 };
    public static final byte[] PROGRAM4 = { (byte)0x10,(byte)0x00 };
    public static final byte[] PROGRAM5 = { (byte)0x20,(byte)0x00 };
    public static final byte[] STOP = { (byte)0x40,(byte)0x00 };
    public static final byte[] SOUND = { (byte)0x80,(byte)0x00 };
    
    ////////////////////////////////////////////
    // fields
    ////////////////////////////////////////////
    /**
     * the tower
     */
    private Tower fTower = null;
    /**
     * the packet (reused)
     */
    private byte[] fPacket = null;
    
    ////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /** 
     * creates a new instance of RemoteControlMessenger 
     */
    public RemoteControlMessenger() {
        // instantiate tower
        fTower = new Tower();
        // instantiate packet
        fPacket = new byte[3];
        // set lego remote opcode
        fPacket[0] = (byte)0xd2;
    } // RemoteControl()
    
    ////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    /**
     * sends a message via the LEGO Tower to the RCX
     * @param int the message to send 
     * (in terms of RemoteControlMessenger constants)
     * @throws IOException if message code is invalid
    */
    public void send(byte[] aMessageCode) throws IOException {
        // ckeck message bytes
        if(aMessageCode.length!=2)
            throw new IOException("invalid message code");
        // build packet
        fPacket[1] = aMessageCode[0];
        fPacket[2] = aMessageCode[1];
        //{(byte){ (byte)0xd2,(byte){ (byte)0x02,(byte){ (byte)0x00};
        sendPacket(fPacket,3);
    } // send()

    ////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////
    
    ////////////////////////////////////////////
    /**
     * sends a message via the LEGO Tower to the RCX
     * @param byte[] the data packet to send
     * @param int the length of the packet
    */
    private void sendPacket(byte[] aPacket,int aPacketLength) {
        // open tower
        fTower.open(); 
        // send packet
        fTower.send(aPacket,aPacketLength);
        // close tower
        fTower.close();
    } // sendPacket()

} // class RemoteControlMessenger
