import josx.util.Test;

public class TestThreadState {
	static class TestThread extends Thread {
		public void run() {
			try {
				sleep(5000);
			} catch (InterruptedException e) {
				Test.assert("Sleep interrupted", true);
			}
		}
	}
	public static void main(String[] args) {
		try {
			Thread t = new TestThread();
			Test.assert("Alive1", !t.isAlive());
			t.start();
			Thread.yield();
			Test.assert("Not alive", t.isAlive());
			t.interrupt();
			Thread.currentThread().sleep(500);
			Test.assert("Alive2", !t.isAlive());
			boolean ex = false;
			try {
				t.start();
			} catch (IllegalStateException ise) {
				ex = true;
			}
			Test.assert("No exception", ex);
			ex = false;
			try  {
				// Run out of memory whilst creating threads:
				while (true) {
					t = new TestThread();
					t.start();
					Thread.yield();
				}
			} catch (OutOfMemoryError oome) {
				ex = true;
			}
			Test.assert("No OutOfMemoryError", ex);
		} catch (Throwable e) {
			System.exit(1);
		}
	}
}