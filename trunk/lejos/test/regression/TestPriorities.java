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
						Test.assertEQ("", 2, ++i);
						print = false;
					}
				}
			}
			try  {
				Test.assertEQ("", 10, ++i);
				sleep(10000);
				// Should never happen
				Test.assert("Bad 1", false);
			} catch (InterruptedException ie) {
				Test.assertEQ("", 13, ++i);
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
						Test.assertEQ("", 5, ++i);
						print = false;
					}
				}
			}
			try  {
				Test.assertEQ("", 16, ++i);
				sleep(10000);
				Test.assert("Bad 2", false);
			} catch (InterruptedException ie) {
				Test.assertEQ("", 17, ++i);
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
						Test.assertEQ("", 18, ++i);
						print = false;
					}
				}
			}
			try  {
				Test.assertEQ("", 23, ++i);
				sleep(10000);
				Test.assertEQ("", 27, ++i);
			} catch (InterruptedException ie) {
				// Should never happen
				Test.assert("Bad 3", false);
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
						Test.assert("Bad 4", false);
						print = false;
					}
				}
			}
			try  {
				Test.assertEQ("", 24, ++i);
				sleep(10000);
				// Should never happen
				Test.assert("Bad 5", false);
			} catch (InterruptedException ie) {
				Test.assertEQ("", 25, ++i);
			}
		}
	}

	public void runTest() {
		i = 99;
		Test.assertEQ("", 100, ++i);
		hp1 = new HighPriority1();
		Test.assertEQ("", 101, ++i);
		hp2 = new HighPriority2();
		lp1 = new LowPriority1();
		lp2 = new LowPriority2();
		Test.assertEQ("", 102, ++i);
		Thread t = Thread.currentThread();
		Test.assertEQ("", 103, ++i);
		int p = t.getPriority();
		Test.assertEQ("", 104, ++i);
		hp1.setPriority(p-1);
		Test.assertEQ("", 105, ++i);
		hp2.setPriority(Thread.currentThread().getPriority()-1);
		lp1.setPriority(Thread.currentThread().getPriority()-2);
		lp2.setPriority(Thread.currentThread().getPriority()-3);
		Test.assertEQ("", 106, ++i);

		i = -1;		
		// We are higher priority than any of them
		// so none of them will run until we sleep.
		hp1.start(); hp2.start(); lp1.start(); lp2.start();
		Test.assertEQ("", 0, ++i);
		hp1.setPrint(true);
		Test.assertEQ("", 1, ++i);
		try {
			Thread.sleep(1000);
			Test.assertEQ("", 3, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 6", false);
		}
		hp2.setPrint(true);
		Test.assertEQ("", 4, ++i);
		try {
			Thread.sleep(1000);
			Test.assertEQ("", 6, ++i);	
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 7", false);
		}
		// This should not print anything since hp1 and hp2 are still runnable
		lp1.setPrint(true);
		Test.assertEQ("Test", 7, ++i);	
		try {
			Thread.sleep(1000);
			Test.assertEQ("", 8, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 8", false);	
		}
		// So now stop them looping and test that we can interrupt them
		hp1.printing = false;
		// Allow them to execute
		Test.assertEQ("", 9, ++i);
		try {
			Thread.sleep(1000);
			// It should be sleeping now, so interrupt it.
			Test.assertEQ("", 11, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 9", false);	
		}
		hp1.interrupt();
		Test.assertEQ("", 12, ++i);
		try {
			Thread.sleep(1000);
			// hp1 should have terminated
			Test.assertEQ("", 14, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 10", false);	
		}
		hp2.interrupt();
		Test.assertEQ("", 15, ++i);
		try {
			Thread.sleep(1000);
			// hp2 should have terminated
			// lp1 should have run and printed
			Test.assertEQ("", 19, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 11", false);	
		}
		// Allow lp1 to sleep
		lp1.printing = false;
		Test.assertEQ("", 20, ++i);
		lp2.interrupt();
		Test.assertEQ("", 21, ++i);
		lp2.printing = false;	// Should never print;
		Test.assertEQ("", 22, ++i);
		try {
			Thread.sleep(2000);
			// lp1 should sleep
			// lp2 should wake up and terminate immediately,
			Test.assertEQ("", 26, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 12", false);	
		}
		// Sleep for a long time to let lp1 terminate normally
		try {
			Thread.sleep(11000);
			Test.assertEQ("", 28, ++i);
		} catch (InterruptedException ie) {
			// Shouldn't happen
			Test.assert("Bad 13", false);	
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
