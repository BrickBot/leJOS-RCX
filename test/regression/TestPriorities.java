import josx.util.*;

public class TestPriorities {
	int i;
	HighPriority1 hp1;
	HighPriority2 hp2;
	LowPriority1 lp1;
	LowPriority2 lp2;

	class HighPriority1 extends Thread {
		boolean print = false;
		boolean printing = true;
		
		public synchronized void setPrint(boolean print) {
			this.print = print;
		}
		
		public void run() {
			while (printing) {
				synchronized(this)  {
					if (print) {
						Assertion.testEQ("", 2, ++i);
						print = false;
					}
				}
			}
			try  {
				Assertion.testEQ("", 10, ++i);
				sleep(10000);
				// Should never happen
				Assertion.test("Bad 1", false);
			} catch (InterruptedException ie) {
				Assertion.testEQ("", 13, ++i);
			}
		}
	}
	
	class HighPriority2 extends Thread {
		boolean print = false;
		
		public synchronized void setPrint(boolean print) {
			this.print = print;
		}
		
		public void run() {
			while (!isInterrupted()) {
				synchronized(this)  {
					if (print) {
						Assertion.testEQ("", 5, ++i);
						print = false;
					}
				}
			}
			try  {
				Assertion.testEQ("", 16, ++i);
				sleep(10000);
				Assertion.test("Bad 2", false);
			} catch (InterruptedException ie) {
				Assertion.testEQ("", 17, ++i);
			}
		}
	}
	
	class LowPriority1 extends Thread {
		boolean print = false;
		boolean printing = true;
		
		public synchronized void setPrint(boolean print) {
			this.print = print;
		}
		
		public void run() {
			while (printing) {
				synchronized(this)  {
					if (print) {
						Assertion.testEQ("", 18, ++i);
						print = false;
					}
				}
			}
			try  {
				Assertion.testEQ("", 23, ++i);
				sleep(10000);
				Assertion.testEQ("", 27, ++i);
			} catch (InterruptedException ie) {
				// Should never happen
				Assertion.test("Bad 3", false);
			}
		}
	}
	
	class LowPriority2 extends Thread {
		boolean print = false;
		boolean printing = true;
		
		public synchronized void setPrint(boolean print) {
			this.print = print;
		}
		
		public void run() {
			while (printing) {
				synchronized(this)  {
					if (print) {
						Assertion.test("Bad 4", false);
						print = false;
					}
				}
			}
			try  {
				Assertion.testEQ("", 24, ++i);
				sleep(10000);
				// Should never happen
				Assertion.test("Bad 5", false);
			} catch (InterruptedException ie) {
				Assertion.testEQ("", 25, ++i);
			}
		}
	}

	public void runTest() {
		i = 99;
		Assertion.testEQ("", 100, ++i);
		hp1 = new HighPriority1();
		Assertion.testEQ("", 101, ++i);
		hp2 = new HighPriority2();
		lp1 = new LowPriority1();
		lp2 = new LowPriority2();
		Assertion.testEQ("", 102, ++i);
		Thread t = Thread.currentThread();
		Assertion.testEQ("", 103, ++i);
		int p = t.getPriority();
		Assertion.testEQ("", 104, ++i);
		hp1.setPriority(p-1);
		Assertion.testEQ("", 105, ++i);
		hp2.setPriority(Thread.currentThread().getPriority()-1);
		lp1.setPriority(Thread.currentThread().getPriority()-2);
		lp2.setPriority(Thread.currentThread().getPriority()-3);
		Assertion.testEQ("", 106, ++i);

		i = -1;		
		// We are higher priority than any of them
		// so none of them will run until we sleep.
		hp1.start(); hp2.start(); lp1.start(); lp2.start();
		Assertion.testEQ("", 0, ++i);
		hp1.setPrint(true);
		Assertion.testEQ("", 1, ++i);
		try {
			Thread.sleep(1000);
			Assertion.testEQ("", 3, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 6", false);
		}
		hp2.setPrint(true);
		Assertion.testEQ("", 4, ++i);
		try {
			Thread.sleep(1000);
			Assertion.testEQ("", 6, ++i);	
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 7", false);
		}
		// This should not print anything since hp1 and hp2 are still runnable
		lp1.setPrint(true);
		Assertion.testEQ("Test", 7, ++i);	
		try {
			Thread.sleep(1000);
			Assertion.testEQ("", 8, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 8", false);	
		}
		// So now stop them looping and test that we can interrupt them
		hp1.printing = false;
		// Allow them to execute
		Assertion.testEQ("", 9, ++i);
		try {
			Thread.sleep(1000);
			// It should be sleeping now, so interrupt it.
			Assertion.testEQ("", 11, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 9", false);	
		}
		hp1.interrupt();
		Assertion.testEQ("", 12, ++i);
		try {
			Thread.sleep(1000);
			// hp1 should have terminated
			Assertion.testEQ("", 14, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 10", false);	
		}
		hp2.interrupt();
		Assertion.testEQ("", 15, ++i);
		try {
			Thread.sleep(1000);
			// hp2 should have terminated
			// lp1 should have run and printed
			Assertion.testEQ("", 19, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 11", false);	
		}
		// Allow lp1 to sleep
		lp1.printing = false;
		Assertion.testEQ("", 20, ++i);
		lp2.interrupt();
		Assertion.testEQ("", 21, ++i);
		lp2.printing = false;	// Should never print;
		Assertion.testEQ("", 22, ++i);
		try {
			Thread.sleep(2000);
			// lp1 should sleep
			// lp2 should wake up and terminate immediately,
			Assertion.testEQ("", 26, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 12", false);	
		}
		// Sleep for a long time to let lp1 terminate normally
		try {
			Thread.sleep(11000);
			Assertion.testEQ("", 28, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Assertion.test("Bad 13", false);	
		}
	}
	
	public static void main(String[] args) {
		try  {
			new TestPriorities().runTest();
		} catch (Error e) {
			System.exit(1);
		}
	}
}
