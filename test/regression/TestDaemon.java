import josx.util.Assertion;

public class TestDaemon {
	static class NormalThread extends Thread {
		public void run() {		
		}
	}
	
	static class DaemonThread extends Thread {
		DaemonThread(int priority) {
			setPriority(priority);
			setDaemon(true);
		}
		
		public void run() {
			try {
				sleep(10000);
			} catch (InterruptedException ie) {
			}
			Assertion.test("Daemon thread woke up", false);			
		}
	}
	
	public static void main(String[] args) {
		new DaemonThread(Thread.MIN_PRIORITY).start();
		new DaemonThread(Thread.MAX_PRIORITY).start();
		new DaemonThread(Thread.currentThread().getPriority()).start();
		new NormalThread().start();		
	}
}
