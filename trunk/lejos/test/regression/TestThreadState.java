import josx.util.Assertion;

public class TestThreadState {
	static class TestThread extends Thread {
		public void run() {
			try {
				sleep(5000);
			} catch (InterruptedException e) {
				Assertion.test("Sleep interrupted", true);
			}
		}
	}
	public static void main(String[] args) {
		try {
			Thread t = new TestThread();
			Assertion.test("Alive1", !t.isAlive());
			t.start();
			Thread.yield();
			Assertion.test("Not alive", t.isAlive());
			t.interrupt();
			Thread.currentThread().sleep(500);
			Assertion.test("Alive2", !t.isAlive());
			boolean ex = false;
			try {
				t.start();
			} catch (IllegalStateException ise) {
				ex = true;
			}
			Assertion.test("No exception", ex);
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
			Assertion.test("No OutOfMemoryError", ex);
		} catch (Throwable e) {
			System.exit(1);
		}
	}
}
