import josx.util.*;

public class TestMonitor {
	int i =0;

	// Test basic wait/notify	
	public void callWait1() {
		synchronized (this) {
			Test.assertEQ("", 1, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Test.assert("Bad 1", false);
			}
			Test.assertEQ("", 4, ++i);
		}
	}

	public void callNotify1() {
		synchronized (this)  {
			Test.assertEQ("", 2, ++i);
			notify();
			Test.assertEQ("", 3, ++i);
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
				Test.assert("Bad 2", false);
			}
			callNotify1();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Test.assert("Bad 3", false);
			}
			Test.assertEQ("", 5, ++i);
		}
	}

	// Test a wait that has a monitor count > 1	
	public synchronized void callWait2() {
		Test.assertEQ("", 6, ++i);
		try  {
			// Should drop monitor_count to zero.
			wait();
		} catch (InterruptedException ie) {
			Test.assert("Bad 4", false);
		}
		Test.assertEQ("", 9, ++i);
	}

	public synchronized void m1() {
		callWait2();
	}
			
	public synchronized void callNotify2() {
		Test.assertEQ("", 7, ++i);
		notify();
		Test.assertEQ("", 8, ++i);
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
				Test.assert("Bad 5", false);
			}
			callNotify2();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Test.assert("Bad 6", false);
			}
			Test.assertEQ("", 10, ++i);
		}
	}

	// Test an interrupted wait	
	public void callWait3() {
		synchronized (this) {
			Test.assertEQ("", 15, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Test.assertEQ("", 16, ++i);
			}
			Test.assertEQ("", 17, ++i);
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
			Test.assertEQ("", 21, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Test.assert("Bad T6.1", false);
			}
			Test.assertEQ("", 26, ++i);
		}
	}

	public void callWait7() {
		synchronized (this) {
			Test.assertEQ("", 22, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Test.assert("Bad T7", false);
			}
			Test.assertEQ("", 25, ++i);
		}
	}

	public void callNotify8() {
		synchronized (this)  {
			Test.assertEQ("", 23, ++i);
			notifyAll();
			Test.assertEQ("", 24, ++i);
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
				Test.assert("Bad T8.1", false);
			}
			callNotify8();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Test.assert("Bad T8.2", false);
			}
			Test.assertEQ("", 27, ++i);
		}
	}

	// Test wait with timeout	
	public void callWait9() {
		synchronized (this) {
			Test.assertEQ("", 29, ++i);
			try  {
				wait(500);
			} catch (InterruptedException ie) {
				Test.assert("Bad 1", false);
			}
			Test.assertEQ("", 30, ++i);
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
			Test.assertEQ("", 32, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Test.assert("Bad T10.1", false);
			}
			Test.assert("Bad T10.2", false);
		}
	}

	public void callWait11() {
		synchronized (this) {
			Test.assertEQ("", 33, ++i);
			try  {
				wait();
			} catch (InterruptedException ie) {
				Test.assert("Bad T11", false);
			}
			Test.assertEQ("", 36, ++i);
		}
	}

	public void callNotify12() {
		synchronized (this)  {
			Test.assertEQ("", 34, ++i);
			notify();
			Test.assertEQ("", 35, ++i);
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
				Test.assert("Bad T12.1", false);
			}
			callNotify12();
			try  {
				sleep(1000); 
			} catch (InterruptedException ie) {
				Test.assert("Bad T12.2", false);
			}
			Test.assertEQ("", 37, ++i);
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
			Test.assert("Bad 7", false);	
		}
		Thread t3 = new T3();
		Thread t4 = new T4();
		t3.setDaemon(true);
		t3.start();
		t4.start();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			Test.assert("Badone", false);	
		}
		
		synchronized(this) {
			try {
				t1.wait();
			} catch (InterruptedException ie) {
				Test.assert("Bad 8", false);
			} catch (IllegalMonitorStateException e) {
				Test.assertEQ("", 11, ++i);
			}
		}
		Test.assertEQ("", 12, ++i);
		
		synchronized(this) {
			try {
				t1.notify();
			} catch (IllegalMonitorStateException e) {
				Test.assertEQ("", 13, ++i);
			}
		}
		Test.assertEQ("", 14, ++i);
		
		Thread t5 = new T5();
		t5.setDaemon(true);
		t5.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Test.assert("Bad 10", false);	
		}
		t5.interrupt();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Test.assert("Bad 11", false);	
		}
		Test.assertEQ("", 18, ++i);
		synchronized(this)  {
			Test.assertEQ("", 19, ++i);
			notify();
			Test.assertEQ("", 20, ++i);
		}
		
		Thread t6 = new T6();
		Thread t7 = new T7();
		Thread t8 = new T8();
		t6.setDaemon(true);
		t6.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Test.assert("Bad T6", false);	
		}
		t7.setDaemon(true);
		t7.start();
		t8.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			Test.assert("Bad T8.3", false);	
		}
		Test.assertEQ("", 28, ++i);
		
		Thread t9 = new T9();
		t9.setDaemon(true);
		t9.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			Test.assert("Bad 7", false);	
		}
		Test.assertEQ("", 31, ++i);
			
		Thread t10 = new T10();
		Thread t11 = new T11();
		Thread t12 = new T12();
		t10.setDaemon(true);
		t10.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			Test.assert("Bad T10", false);	
		}
		t11.setDaemon(true);
		t11.start();
		t12.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			Test.assert("Bad T12.3", false);	
		}
		Test.assertEQ("", 38, ++i);
	}
	
	public static void main(String[] args) {
		try  {
			new TestMonitor().runTest();
		} catch (Error e) {
			System.exit(1);
		}
	}
}