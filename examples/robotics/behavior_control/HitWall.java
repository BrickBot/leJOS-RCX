import josx.robotics.*;
import josx.platform.rcx.*;

public class HitWall implements Behavior {

   public boolean takeControl() {
      return Sensor.S2.readBooleanValue();
   }
   
   public void suppress() {
      Motor.A.stop();
      Motor.C.stop();
   }
   
   public void action() {
      // Back up:
      Motor.A.backward();
      Motor.C.backward();
      try{Thread.sleep(1000);}catch(Exception e) {}
      // Rotate by causing only one wheel to stop:
      Motor.A.stop();
      try{Thread.sleep(300);}catch(Exception e) {}
      Motor.C.stop();
   }
}