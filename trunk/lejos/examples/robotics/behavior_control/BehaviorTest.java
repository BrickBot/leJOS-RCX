import josx.robotics.*;

/**
 * Behavior Control example
 *  * @author <a href="mailto:bbagnall@escape.ca">Brian Bagnall</a>
 */
class BumperCar {
   public static void main(String [] args) {
      Behavior b1 = new DriveForward();
      Behavior b2 = new HitWall();
      // BatteryLow doesn't work well so I excluded it
      //Behavior b3 = new BatteryLow(6.0f);
      
      // NOTE: low level behaviors should have lower index number
      // in the array i.e. b2 is higher level than b1.
      Behavior [] bArray = {b1, b2};
      //Behavior [] bArray = {b1, b2, b3};
      
      Arbitrator arby = new Arbitrator(bArray);
      arby.start();
   }   
}