import josx.platform.rcx.*;

/**
 * 
 */

public class SerialListenerTest implements Opcode
{   
  public static void main( String[] arg)
    throws InterruptedException
  {
    Serial.addSerialListener (new SerialListener()
      {
        public void packetAvailable(byte[] packet, int length)
        {
          if ( (packet[0] & 255) != (OPCODE_REMOTE_COMMAND & 255)) {
            return;
          }

          int c = (packet[1] & 255) + (packet[2] & 255) * 256;
          for ( int i=0; i<16 ; i++) {
            if ( ((c>>i) & 1) == 1) {
              LCD.showNumber (i);
              break;
            }
          }
        }
      });
    Button.RUN.waitForPressAndRelease();
    System.exit( 0);
  }
}

