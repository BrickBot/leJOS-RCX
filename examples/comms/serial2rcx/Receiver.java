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
     
   private byte[] buffer = new byte[10];
   
   /**
    * Receive a single byte
    */
   protected byte receiveByte() {
      josx.platform.rcx.Serial.readPacket(buffer);
      return buffer[1];
   }  
}