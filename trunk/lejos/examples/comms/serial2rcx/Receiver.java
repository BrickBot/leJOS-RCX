import josx.platform.rcx.*;

public class Receiver {

   
   public static void main(String [] args) throws Exception {
      Receiver r = new Receiver();
      
      for(;;) {
         if(Serial.isPacketAvailable()) {
            Sound.beep();
            LCD.showNumber(r.receiveByte());
         }
      }
   }
   
   private byte[] packet = {(byte)0xf7, (byte)0x00};
   
   /**
    * Send a single byte
    */
   protected void sendByte(byte b) throws Exception {
      packet[1] = b;
      josx.platform.rcx.Serial.sendPacket(packet, 0, 2);
   }
  
   private byte[] buffer = new byte[10];
   
   /**
    * Receive a single byte
    */
   protected byte receiveByte() throws Exception {
      josx.platform.rcx.Serial.readPacket(buffer);
      return buffer[1];
   }  
}