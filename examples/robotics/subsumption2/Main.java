import josx.platform.rcx.*;

/**
 * Entry point for the program. Creates an instance of Subsumption
 * and kicks of the lowest priority task (wander). This version uses
 * the enhanced Thread interface available in lejos1.0.2
 */
public class Main  {
	public static final long FOREVER = 0x7fffffff;
	
	public static Subsumption main;
	public static void main (String[] arg)
	  throws Exception {
	  	main = new Subsumption();
		main.start();
		main.tasks[0].execute();
	}
}

/**
 * Coordinates a prioritised set of tasks. Tasks with higher priority
 * completely block those of a lower priority. Creates the following
 * tasks with increasing order of priority:
 * - Wander:      wander around
 * - RightBumber: avoid obstacles on the right.
 * - LeftBumber:  avoid obstacles on the left.
 * Plus it gates access to the effectors (i.e. the motors) ensuring
 * that it's priority policy is strictly enforced.
 */
class Subsumption extends Thread implements ButtonListener {
	public Task owner;
	Task tasks[];
	
	public Subsumption() {
		Button.RUN.addButtonListener(this);
		tasks = new Task[3];
		tasks[0]=new Wander();
		tasks[0].setPriority(Thread.MIN_PRIORITY);
		tasks[0].setDaemon(true);
		
		tasks[1]=new RightBumber();
		tasks[1].setPriority(Thread.MIN_PRIORITY+1);
		tasks[1].setDaemon(true);
		
		tasks[2]=new LeftBumber();
		tasks[2].setPriority(Thread.MIN_PRIORITY+1);
		tasks[2].setDaemon(true);
	}

	public void run() {
		tasks[0].start();
		tasks[1].start();
		tasks[2].start();
		try {
			sleep(Main.FOREVER);
		} catch (InterruptedException ie) {
		}
	}
	
	/**
	 * Arbitrates between the various tasks.
	 */	
	public synchronized void execute(Task requestor) {
		// Start new owner from beginning (even if it was the same one)
		owner = requestor;	
		requestor.reset();
	}

	/**
	 * Only allow the owner to do stuff, just in case some other task calls us.
	 * If every task had a different priority we wouldn't need to do this
	 * but as 
	 */	
	public synchronized void setMotor(Task requestor, Motor motor, int power, boolean forward) {
		if (owner == requestor) {
			motor.setPower(power);
			if (forward)
				motor.forward();
			else
				motor.backward();
		}
	}

	/**
	 * Task has finished. Re-start next runnable task from the beginning.
	 */	
	public synchronized void release(Task releaser) {
		// If it isn't the owner releasing, ignore it.
		if (owner == releaser) {
			// Search for the first runnable task. There is always one.
			for(int i=tasks.length-1; i >= 0; i--) {
				if (tasks[i].running()) {
					owner = tasks[i];
					owner.reset();
					break;
				}	
			}
		}
	}

	public void buttonPressed(Button b) {
	}
		
	/**
	 * If the 'run' button is pressed and released, quit.
	 */			
	public void buttonReleased(Button b) {
		interrupt();
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
 * All tasks (wander, leftbumber, rightbumber) extend this class. This class
 * defines the lifecycle of a task. Namely:
 * Reset:   Re-initialise task
 * Execute: Attempt to run (may not succeed if a higher priority task is running).
 * Run:     Execute an FSM.
 * Release: Stop running.
 */
abstract class Task extends Thread implements SensorConstants {
	public static final Motor LEFT_MOTOR = Motor.C;
	public static final Motor RIGHT_MOTOR = Motor.A;
	public static final Sensor LEFT_BUMBER = Sensor.S3;
	public static final Sensor RIGHT_BUMBER = Sensor.S1;
	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final int END = -1;
	public static final int START = 0;
	
	public static int tcount = 0;
	
	public int task;
	
	public Action actions[];
	public int fsm[];
	public int state = END;

	public Task() {
		task = ++tcount;
	}

	/**
	 * Reset the FSM to its initial state.
	 */	
	public void reset() {
		synchronized (Main.main) {
			state = START;
			
			// Wake this thread up (not the current thread);
			interrupt();
		}
	}

	/**
	 * The thread entry point. Either runs the action's FSM to completion -
	 * sleeping or yielding between each state - or until 'running' is false.
	 * When finished call 'release'.
	 * <P>
	 * FSM is really a bit of a misnomer as there are no input events so
	 * there is only one transition from each state to the next.
	 */
	public void run() {
		// Keep running until the program should exit.
		do {
			try {
				MinLCD.setNumber(0x301f,task,0x3002);
				MinLCD.refresh();
				sleep(Main.FOREVER);
			} catch (InterruptedException ie) {
			}
			
			// Execute the FSM until it stops...
			while (state != END) {
				int toSleepFor;
				synchronized (Main.main) {
					MinLCD.setNumber(0x301f,(state+1)*10+task,0x3002);
					MinLCD.refresh();
					toSleepFor = actions[state].act();
					state = fsm[state];
				}
				if (toSleepFor > 0) {
					try {
						sleep(toSleepFor);
					} catch (InterruptedException ie) {
					}
				}
				else	
					yield();
			}
			
			// Its over, release the actuators.
			release();
		} while (true);
	}

	/**
	 * Inform the coordinator that we have released the actuators.
	 */	
	public void release()  {
		Main.main.release(this);
	}

	/**
	 * Request control of the actuators
	 */	
	public void execute() {
		if (Main.main != null)
			Main.main.execute(this);
	}

	/**
	 * Return true if the FSM is executing, false otherwise.
	 */
	public boolean running()  {
		return state != END;
	}
	
	/**
	 * Convenience function to make it appear to subclasses that
	 * they have direct control of the actuators when they are in
	 * fact gated by the controller.
	 */	
	public void setMotor(Motor motor, int power, boolean forward) {
		Main.main.setMotor(this, motor, power, forward);
	}
}

/**
 * Defines a finite state machine to avoid an obstacle on the left.
 */		
class LeftBumber extends Task implements SensorListener {
	public LeftBumber() {
		LEFT_BUMBER.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_BOOL);
		LEFT_BUMBER.activate();
		actions = new Action[2];
		actions[0] = new Action() {
			public int act() {
				setMotor(LEFT_MOTOR, 7, BACKWARD);
				setMotor(RIGHT_MOTOR, 7, BACKWARD);
				return 200;
			}
		};
		
		actions[1] = new Action() {
			public int act() {
				setMotor(LEFT_MOTOR, 7, FORWARD);
				return 200;
			}
		};

		fsm = new int[2];		
		fsm[0] = 1;
		fsm[1] = END;
		LEFT_BUMBER.addSensorListener(this);
	}

	/**
	 * This is actually executed in a thread established by
	 * LEFT_BUMBER.addSensorListener().
	 */	
	public void stateChanged(Sensor bumber, int oldValue, int newValue) {
		Sound.playTone(440, 10);
		if (bumber.readBooleanValue()) {
			Sound.playTone(500, 10);
			execute();
		}
	}
}

/**
 * Defines a finite state machine to avoid an obstacle on the right.
 */		
class RightBumber extends Task implements SensorListener {
	public RightBumber() {
		RIGHT_BUMBER.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_BOOL);
		RIGHT_BUMBER.activate();
		actions = new Action[2];
		actions[0] = new Action() {
			public int act() {
				setMotor(LEFT_MOTOR, 7, BACKWARD);
				setMotor(RIGHT_MOTOR, 7, BACKWARD);
				return 200;
			}
		};
		
		actions[1] = new Action() {
			public int act() {
				setMotor(RIGHT_MOTOR, 7, FORWARD);
				return 200;
			}
		};

		fsm = new int[2];		
		fsm[0] = 1;
		fsm[1] = END;
		RIGHT_BUMBER.addSensorListener(this);
	}

	/**
	 * This is actually executed in a thread established by
	 * RIGHT_BUMBER.addSensorListener().
	 */	
	public void stateChanged(Sensor bumber, int oldValue, int newValue) {
		Sound.playTone(1000, 10);
		if (bumber.readBooleanValue()) {
			Sound.playTone(1400, 10);
			execute();
		}
	}
}

/**
 * Defines a finite state machine to wander around aimlessley.
 */		
class Wander extends Task {
	public Wander() {
		actions = new Action[3];
		actions[0] = new Action() {
			public int act() {
				setMotor(LEFT_MOTOR, 7, FORWARD);
				setMotor(RIGHT_MOTOR, 7, FORWARD);
				return 5000;
			}
		};
		
		actions[1] = new Action() {
			public int act() {
				setMotor(LEFT_MOTOR, 3, FORWARD);
				setMotor(RIGHT_MOTOR, 7, FORWARD);
				return 2000;
			}
		};
		
		actions[2] = new Action() {
			public int act() {
				setMotor(LEFT_MOTOR, 7, BACKWARD);
				setMotor(RIGHT_MOTOR, 7, FORWARD);
				return 700;
			}
		};
		
		fsm = new int[3];		
		fsm[0] = 1;
		fsm[1] = 2;
		fsm[2] = 0;
	}
}
