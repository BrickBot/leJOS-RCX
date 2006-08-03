import josx.platform.rcx.*;

class Sender
{
   public static void main(String [] args)  throws InterruptedException {
      Sender s = new Sender();
      for(byte i=0;i<200;++i) {
         Button.RUN.waitForPressAndRelease();
         Sound.beep();
         s.sendByte(i);
      }
   }
   
   private byte[] packet = {(byte)0xf7, (byte)0x00};
   
   /**
    * Send a single byte
    */
   protected void sendByte(byte b) {
      packet[1] = b;
      josx.platform.rcx.Serial.sendPacket(packet, 0, 2);
   }
}