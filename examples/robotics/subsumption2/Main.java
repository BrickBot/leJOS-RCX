import josx.platform.rcx.*;

/**
 * Entry point for the program. This version uses
 * the enhanced Thread interface available in lejos1.0.2
 */
public class Main implements ButtonListener {
	public static void main (String[] arg)
	  throws Exception {
	  	try  {
	  		new Main().start();
	  	} catch (OutOfMemoryError e)  {
		  	MinLCD.setNumber(0x301f,(int)Runtime.getRuntime().totalMemory(),0x3002);
		  	MinLCD.refresh();
	  	}
	  }
	  
	public void start() {
	  	Button.RUN.addButtonListener(this);

	  	Sense s1 = new SenseNoOwner(new Wander());
		s1.setPri(Thread.MIN_PRIORITY);
		Sense s2 = new SenseBumper(Sensor.S3, new AvoidLeft());
		s2.setPri(Thread.MIN_PRIORITY+1);
		Sense s3 = new SenseBumper(Sensor.S1, new AvoidRight());
		s3.setPri(Thread.MIN_PRIORITY+1);
	
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		s1.runIt();
		s2.runIt();	
		s3.runIt();

		MinLCD.setNumber(0x301f,(int)Runtime.getRuntime().freeMemory(),0x3002);
		MinLCD.refresh();

		// Wait until RUN button is pressed.		
		synchronized (Button.RUN) {
			try  {
				Button.RUN.wait();
			} catch (InterruptedException ie) {
			}
		}
	}
	
	public void buttonPressed(Button b) {
	}
		
	/**
	 * If the 'run' button is pressed and released, quit.
	 */			
	public void buttonReleased(Button b) {
		synchronized (b) {
			// Wake the wait() up.
			b.notifyAll();
		}
	}
}

/**
 * Functor interface. Or, to put it another way, the interface to
 * actions stored in a finite state machine (fsm).
 */
interface Action {
	public int act();
}

/**
 * A runnable instance of an FSM,
 */
abstract class Actuator extends Thread {
	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final int END = -1;
	public static final int START = 0;

	// Any old object will do as the 'arbitrator'
	static Object arbitrator = new Object();
	
	// This must be set to the one actuator allowed to execute whilst the
	// monitor of 'arbitrator' is owned.
	static Actuator owner;
	
	protected Action actions[];
	protected int fsm[];
	protected int state = END;
	
	// Useful for debugging.	
	public static int tcount = 0;
	public int task;

	public Actuator() {
		task = ++tcount;
		setDaemon(true);
	}
	
	/**
	 * The thread entry point. Runs the Actuator's FSM to completion -
	 * waiting on the arbitrator's monitor between each state.
	 * <P>
	 * FSM is really a bit of a misnomer as there are no input events so
	 * there is only one transition from each state to the next.
	 */
	public void run() {
		MinLCD.setNumber(0x301f,task,0x3002);
		MinLCD.refresh();

		// Keep running until the program should exit.
		synchronized (arbitrator) {
			do {
				// Wait until we get ownership.
				while (owner != this) {
					try  {
						arbitrator.wait();	// Release arbitrator until notified
					} catch (InterruptedException ie) {
					}
				}
				
				// Set state to start because we might have been terminated
				// prematurely and we always start from the beginning.
				state = START;
				
				// Loop until we end or we loose ownership.				
				while (owner == this && state != END) {
					MinLCD.setNumber(0x301f,(state+1)*10+task,0x3002);
					MinLCD.refresh();
					try  {
						// Call wait() because it releases the arbitrator.
						arbitrator.wait(actions[state].act());
					} catch (InterruptedException ie) {
					}
					state = fsm[state];
				}

				// If we ran to completion signify no owner.				
				if (state == END)
					owner = null;
				
				arbitrator.notifyAll();	
			} while (true);
		}
	}

	/**
	 * Run the Actuator.
	 */	
	public void execute() {
		synchronized (arbitrator) {
			// Basically, set a global flag that all threads can test
		// to see if they should stop running their FSM.
			owner = this;
			
			// Wake up anything waiting on 'arbitrator'.
			arbitrator.notifyAll();
		}
	}
}

/**
 * Base class for sensor listener thread. This is tightly coupled to
 * an actuator in this implementation.
 */
abstract class Sense extends Thread implements SensorListener, SensorConstants {
	Actuator actuator;
	
	Sense(Actuator actuator) {
		this.actuator = actuator;
		setDaemon(true);
	}

	/**
	 * This is actually executed in a thread established by
	 * &lt;bumper&gt;.addSensorListener(). That thread executes at
	 * MAX_PRIORITY so just hand the call off.
	 */	
	public void stateChanged(Sensor bumper, int oldValue, int newValue) {
		synchronized (bumper) {
			bumper.notifyAll();
		}
	}
	
	public void setPri(int priority) {
		actuator.setPriority(priority);
		setPriority(priority);
	}
	
	public void runIt() {
		actuator.start();
		start();
	}
}

/**
 * Defines a thread to detect an obstacle on the left.
 */
class SenseBumper extends Sense {
	Sensor bumper;

	SenseBumper(Sensor bumper, Actuator actuator) {
		super(actuator);

		this.bumper = bumper;
		bumper.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_BOOL);
		bumper.activate();
		
		// Add a listener for the bumper
		bumper.addSensorListener(this);
	}
	
	public void run() {
		// Never exit the thread
		do {
			// Grab the monitor of the bumper
			synchronized (bumper) {
			
				// While bumper isn't pressed wait.
				while (!bumper.readBooleanValue()) {
					try {
						bumper.wait();
					} catch (InterruptedException ie) {
					}
					Sound.playTone(440, 10);
				}
			}
			Sound.playTone(500, 10);
			
			// Execute our FSM
			actuator.execute();
		} while (true);
	}
}

/**
 * Defines a finite state machine to avoid an obstacle on the left.
 */		
class AvoidLeft extends Actuator {
	public AvoidLeft() {
		actions = new Action[2];
		actions[0] = new Action() {
			public int act() {
				Motor.C.setPower(7); Motor.C.backward();
				Motor.A.setPower(7); Motor.A.backward();
				return (200);
			}
		};
		
		actions[1] = new Action() {
			public int act() {
				Motor.C.forward();
				return (200);
			}
		};

		fsm = new int[2];		
		fsm[0] = 1;
		fsm[1] = END;
	}
}

/**
 * Defines a finite state machine to avoid an obstacle on the right.
 */		
class AvoidRight extends Actuator {
	public AvoidRight() {
		actions = new Action[2];
		actions[0] = new Action() {
			public int act() {
				Motor.C.setPower(7); Motor.C.backward();
				Motor.A.setPower(7); Motor.A.backward();
				return (200);
			}
		};
		
		actions[1] = new Action() {
			public int act() {
				Motor.A.forward();
				return (200);
			}
		};

		fsm = new int[2];		
		fsm[0] = 1;
		fsm[1] = END;
	}
}

/**
 * A class to sense when the arbitrator has no owner so we can give it one.
 */
class SenseNoOwner extends Sense  {
	public SenseNoOwner(Actuator actuator) {
		super(actuator);
	}
	
	public void run() {
		while (true) {
			synchronized (Actuator.arbitrator) {
				try {
					Actuator.arbitrator.wait();	// Wait until notified
				} catch (InterruptedException ie) {
				}
				
				// If there is no owner, we'll take it.
				if (Actuator.owner == null)
					actuator.execute();
			}	
		}
	}
}

/**
 * Defines a finite state machine to wander around aimlessley.
 */		
class Wander extends Actuator {
	public Wander() {
		actions = new Action[3];
		actions[0] = new Action() {
			public int act() {
				Motor.C.setPower(7); Motor.C.forward();
				Motor.A.setPower(7); Motor.A.forward();
				return (5000);
			}
		};
		
		actions[1] = new Action() {
			public int act() {
				Motor.C.setPower(3);
				return (2000);
			}
		};
		
		actions[2] = new Action() {
			public int act() {
				Motor.C.setPower(7); Motor.C.backward();
				return (700);
			}
		};
		
		fsm = new int[3];		
		fsm[0] = 1;
		fsm[1] = 2;
		fsm[2] = 0;
	}
}
