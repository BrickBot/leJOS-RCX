import josx.util.*;

public class TestMonitor {
	int i =0;

	// Test basic wait/notify	
	public void callWait1() {
		synchronized (this) {
			Assertion.testEQ("", 1, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Assertion.test("Bad 1", false);
			}
			Assertion.testEQ("", 4, ++i);
		}
	}

	public void callNotify1() {
		synchronized (this)  {
			Assertion.testEQ("", 2, ++i);
			notify();
			Assertion.testEQ("", 3, ++i);
		}
	}

	class T1 extends Thread  {
		public void run() {
			callWait1();
		}
	}
	
	class T2 extends Thread  {
		public void run() {
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad 2", false);
			}
			callNotify1();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad 3", false);
			}
			Assertion.testEQ("", 5, ++i);
		}
	}

	// Test a wait that has a monitor count > 1	
	public synchronized void callWait2() {
		Assertion.testEQ("", 6, ++i);
		try  {
			// Should drop monitor_count to zero.
			wait();
		} catch (InterruptedException ie) {
			Assertion.test("Bad 4", false);
		}
		Assertion.testEQ("", 9, ++i);
	}

	public synchronized void m1() {
		callWait2();
	}
			
	public synchronized void callNotify2() {
		Assertion.testEQ("", 7, ++i);
		notify();
		Assertion.testEQ("", 8, ++i);
	}
	
	class T3 extends Thread  {
		public void run() {
			m1();
		}
	}
	
	class T4 extends Thread  {
		public void run() {
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad 5", false);
			}
			callNotify2();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad 6", false);
			}
			Assertion.testEQ("", 10, ++i);
		}
	}

	// Test an interrupted wait	
	public void callWait3() {
		synchronized (this) {
			Assertion.testEQ("", 15, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Assertion.testEQ("", 16, ++i);
			}
			Assertion.testEQ("", 17, ++i);
		}
	}

	class T5 extends Thread  {
		public void run() {
			callWait3();
		}
	}

	// Test notify all	
	public void callWait6() {
		synchronized (this) {
			Assertion.testEQ("", 21, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Assertion.test("Bad T6.1", false);
			}
			Assertion.testEQ("", 26, ++i);
		}
	}

	public void callWait7() {
		synchronized (this) {
			Assertion.testEQ("", 22, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Assertion.test("Bad T7", false);
			}
			Assertion.testEQ("", 25, ++i);
		}
	}

	public void callNotify8() {
		synchronized (this)  {
			Assertion.testEQ("", 23, ++i);
			notifyAll();
			Assertion.testEQ("", 24, ++i);
		}
	}

	class T6 extends Thread  {
		public void run() {
			setPriority(getPriority()-1);
			callWait6();
		}
	}
	
	class T7 extends Thread  {
		public void run() {
			callWait7();
		}
	}
	
	class T8 extends Thread  {
		public void run() {
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad T8.1", false);
			}
			callNotify8();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad T8.2", false);
			}
			Assertion.testEQ("", 27, ++i);
		}
	}

	// Test wait with timeout	
	public void callWait9() {
		synchronized (this) {
			Assertion.testEQ("", 29, ++i);
			try  {
				wait(500);
			} catch (InterruptedException ie) {
				Assertion.test("Bad 1", false);
			}
			Assertion.testEQ("", 30, ++i);
		}
	}

	class T9 extends Thread  {
		public void run() {
			callWait9();
		}
	}
	
	// Test that notify() only wakes one thread	
	public void callWait10() {
		synchronized (this) {
			Assertion.testEQ("", 32, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Assertion.test("Bad T10.1", false);
			}
			Assertion.test("Bad T10.2", false);
		}
	}

	public void callWait11() {
		synchronized (this) {
			Assertion.testEQ("", 33, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Assertion.test("Bad T11", false);
			}
			Assertion.testEQ("", 36, ++i);
		}
	}

	public void callNotify12() {
		synchronized (this)  {
			Assertion.testEQ("", 34, ++i);
			notify();
			Assertion.testEQ("", 35, ++i);
		}
	}

	class T10 extends Thread  {
		public void run() {
			setPriority(getPriority()-1);
			callWait10();
		}
	}
	
	class T11 extends Thread  {
		public void run() {
			callWait11();
		}
	}
	
	class T12 extends Thread  {
		public void run() {
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad T12.1", false);
			}
			callNotify12();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Assertion.test("Bad T12.2", false);
			}
			Assertion.testEQ("", 37, ++i);
		}
	}

	public void runTest() {
		Thread t1 = new T1();
		Thread t2 = new T2();
		t1.setDaemon(true);
		t1.start();
		t2.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad 7", false);	
		}
		Thread t3 = new T3();
		Thread t4 = new T4();
		t3.setDaemon(true);
		t3.start();
		t4.start();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			Assertion.test("Badone", false);	
		}
		
		synchronized(this) {
			try {
				t1.wait();
			} catch (InterruptedException ie) {
				Assertion.test("Bad 8", false);
			} catch (IllegalMonitorStateException e) {
				Assertion.testEQ("", 11, ++i);
			}
		}
		Assertion.testEQ("", 12, ++i);
		
		synchronized(this) {
			try {
				t1.notify();
			} catch (IllegalMonitorStateException e) {
				Assertion.testEQ("", 13, ++i);
			}
		}
		Assertion.testEQ("", 14, ++i);
		
		Thread t5 = new T5();
		t5.setDaemon(true);
		t5.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad 10", false);	
		}
		t5.interrupt();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad 11", false);	
		}
		Assertion.testEQ("", 18, ++i);
		synchronized(this)  {
			Assertion.testEQ("", 19, ++i);
			notify();
			Assertion.testEQ("", 20, ++i);
		}
		
		Thread t6 = new T6();
		Thread t7 = new T7();
		Thread t8 = new T8();
		t6.setDaemon(true);
		t6.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad T6", false);	
		}
		t7.setDaemon(true);
		t7.start();
		t8.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad T8.3", false);	
		}
		Assertion.testEQ("", 28, ++i);
		
		Thread t9 = new T9();
		t9.setDaemon(true);
		t9.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad 7", false);	
		}
		Assertion.testEQ("", 31, ++i);
			
		Thread t10 = new T10();
		Thread t11 = new T11();
		Thread t12 = new T12();
		t10.setDaemon(true);
		t10.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad T10", false);	
		}
		t11.setDaemon(true);
		t11.start();
		t12.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			Assertion.test("Bad T12.3", false);	
		}
		Assertion.testEQ("", 38, ++i);
	}
	
	public static void main(String[] args) {
		try  {
			new TestMonitor().runTest();
		} catch (Error e) {
			System.exit(1);
		}
	}
}
