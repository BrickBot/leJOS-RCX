package josx.rcxcomm.remotecontrol;

/*
 * $Log$
 * Revision 1.7  2005/11/23 17:46:45  mpscholz
 * minor javadoc related changes
 *
 * Revision 1.6  2005/09/02 19:46:08  markus_heiden
 * mh: added some todos
 *
 * Revision 1.5  2005/03/01 19:09:22  mpscholz
 * throws Unsatisfied link error
 *
 * Revision 1.4  2004/07/20 17:02:34  markus_heiden
 * mh: reworked tower jni implementation
 *
 * Revision 1.3  2004/06/21 22:49:26  markus_heiden
 * reformatted code
 * Revision 1.2 2004/06/12 14:59:27
 * markus_heiden reworked tools
 * 
 * Revision 1.1 2003/05/09 21:43:27 mpscholz moved remote control classes to
 * josx.rcxcomm
 * 
 * Revision 1.1 2003/05/01 12:00:06 mpscholz a messenger which sends LEGO remote
 * control messages
 *  
 */
import java.io.IOException;

import josx.rcxcomm.Tower;
import josx.rcxcomm.TowerException;

/////////////////////////////////////////////////////////
/**
 * 
 * This class is a Messenger for remote control messages. <br>
 * It sends remote control messages; the original LEGO remote control opcodes
 * are used
 * 
 * @author Matthias Paul Scholz (mp.scholz@t-online.de)
 * @version 1.0 (01/05/2003)
 */
public class RemoteControlMessenger
{

   ////////////////////////////////////////////
   // constants
   ////////////////////////////////////////////
   public static final byte[] NONE =
   {
      (byte) 0x00, (byte) 0x00
   };
   public static final byte[] MESSAGE1 =
   {
      (byte) 0x00, (byte) 0x01
   };
   public static final byte[] MESSAGE2 =
   {
      (byte) 0x00, (byte) 0x02
   };
   public static final byte[] MESSAGE3 =
   {
      (byte) 0x00, (byte) 0x04
   };
   public static final byte[] MOTOR_A_UP =
   {
      (byte) 0x00, (byte) 0x08
   };
   public static final byte[] MOTOR_B_UP =
   {
      (byte) 0x00, (byte) 0x10
   };
   public static final byte[] MOTOR_C_UP =
   {
      (byte) 0x00, (byte) 0x20
   };
   public static final byte[] MOTOR_A_DOWN =
   {
      (byte) 0x00, (byte) 0x40
   };
   public static final byte[] MOTOR_B_DOWN =
   {
      (byte) 0x00, (byte) 0x80
   };
   public static final byte[] MOTOR_C_DOWN =
   {
      (byte) 0x01, (byte) 0x00
   };
   public static final byte[] PROGRAM1 =
   {
      (byte) 0x02, (byte) 0x00
   };
   public static final byte[] PROGRAM2 =
   {
      (byte) 0x04, (byte) 0x00
   };
   public static final byte[] PROGRAM3 =
   {
      (byte) 0x08, (byte) 0x00
   };
   public static final byte[] PROGRAM4 =
   {
      (byte) 0x10, (byte) 0x00
   };
   public static final byte[] PROGRAM5 =
   {
      (byte) 0x20, (byte) 0x00
   };
   public static final byte[] STOP =
   {
      (byte) 0x40, (byte) 0x00
   };
   public static final byte[] SOUND =
   {
      (byte) 0x80, (byte) 0x00
   };

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
    * Creates a new instance of RemoteControlMessenger.
    * 
    */
   public RemoteControlMessenger(String port) throws UnsatisfiedLinkError
   {
      // instantiate tower
      fTower = new Tower(port);
      // instantiate packet
      fPacket = new byte[3];
      // set lego remote opcode
      fPacket[0] = (byte) 0xd2;
   } // RemoteControl()

   ////////////////////////////////////////////
   // public methods
   ////////////////////////////////////////////

   ////////////////////////////////////////////
   /**
    * sends a message via the LEGO Tower to the RCX
    * 
    * @param aMessageCode the message to send (in terms of RemoteControlMessenger
    *           constants)
    * @throws IOException 
    */
   public void send (byte[] aMessageCode) throws IOException
   {
      // ckeck message bytes
      if (aMessageCode.length != 2)
         throw new IOException("invalid message code");
      // build packet
      fPacket[1] = aMessageCode[0];
      fPacket[2] = aMessageCode[1];
      //{(byte){ (byte)0xd2,(byte){ (byte)0x02,(byte){ (byte)0x00};
      sendPacket(fPacket, 3);
   } // send()

   ////////////////////////////////////////////
   // private methods
   ////////////////////////////////////////////

   ////////////////////////////////////////////
   /**
    * sends a message via the LEGO Tower to the RCX
    * 
    * @param byte[] the data packet to send
    * @param int the length of the packet
    * @throws IOException 
    */
   private void sendPacket (byte[] aPacket, int aPacketLength) throws IOException
   {
      // TODO rework
      try
      {
         // open tower
         fTower.openTower(false);
         // send packet
         fTower.sendPacket(aPacket);
         // close tower
         fTower.closeTower();
      }
      catch (TowerException e)
      {
      	IOException ioExc = new IOException(e.getMessage());
      	ioExc.fillInStackTrace();
        throw ioExc;
      }
   } // sendPacket()

} // class RemoteControlMessenger
