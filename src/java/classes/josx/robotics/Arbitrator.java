package josx.robotics;

// ** use Behavior currentBehavior !!!!

/**
* Arbitrator controls which behavior should currently be active in 
* a behavior control system. Make sure to call start() after the 
* Arbitrator is instantiated.
* @see robotics.rcx.Behavior
* @author <a href="mailto:bbagnall@escape.ca">Brian Bagnall</a>
* @version 0.1  27-July-2001
*/
public class Arbitrator {
   
   private Behavior [] behavior;
   //private Behavior currentBehavior;
   private int currentBehavior = 99;
   
   /**
   * Allocates an Arbitrator object and initializes it with an array of
   * Behavior objects. The highest index in the Behavior array will have the
   * highest order behavior level, and hence will suppress all lower level
   * behaviors if it becomes active. The Behaviors in an Arbitrator can not
   * be changed once the arbitrator is initialized.<BR>
   * <B>NOTE:</B> Once the Arbitrator is initialized, the method start() must be
   * called to begin the arbitration.
   * @param behavior An array of Behavior objects.
   */
   public Arbitrator(Behavior [] behaviors) {
      this.behavior = behaviors;
   }
   
   /**
   * This method starts the arbitration of Behaviors.
   * Modifying the start() method is not recomended. <BR>
   * Note: Arbitrator does not run in a seperate thread, and hence the start()
   * method will never return.
   */
   public void start() {
      int totalBehaviors = behavior.length - 1;
      while(true) {
         // Check through all behavior.takeControl() starting at highest level behavior
         for(int i = totalBehaviors;i>=0;--i) {
            if(behavior[i].takeControl()) {
               // As soon as takeControl() is true, execute the currentBehavior.suppress()
               //if(behavior[i] != currentBehavior) {
               if(i != currentBehavior) { // Prevents program from running same action over and over again
                  if (currentBehavior != 99)
                     behavior[currentBehavior].suppress();
                  // Make currentBehavior this one
                  //currentBehavior = behavior[i];
                  currentBehavior = i;
                  // ** I'm confused.. keep running this over and over?
                  // ** Or run currentBehavior in its own thread?
                  // Run the currentBehavior.behaviorAction()
                  behavior[currentBehavior].action();
                  break; // Breaks out of for(;;) loop
               }
            }
         }
      }
   }
}