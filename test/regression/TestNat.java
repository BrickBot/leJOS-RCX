import josx.platform.rcx.*;
import josx.util.Test;

public class TestNat
{
	class T1 extends Thread
	{
		public void run()
		{
			Poll poller = new Poll();
			int oldValue = Sensor.S3.readValue();
			for (int i=0; i<10; i++)
			{
				try {
					int changed = poller.poll(Poll.SENSOR3_MASK, 0);
					int newValue = Sensor.S3.readValue();
					Test.assert("Values are equal!", oldValue != newValue);
					Test.assert("Mask is invalid!", changed == Poll.SENSOR3_MASK);
					oldValue = newValue;
				} catch (InterruptedException ie) {
					Test.assert("Thread interrupted", false);
				}
			}
		}
	}

	class T2 extends Thread
	{
		public void run()
		{
			Poll poller = new Poll();
			int oldValue = Sensor.S2.readValue();
			for (int i=0; i<20; i++)
			{
				try {
					int changed = poller.poll(Poll.SENSOR2_MASK, 0);
					int newValue = Sensor.S2.readValue();
					Test.assert("Values are equal!", oldValue != newValue);
					Test.assert("Mask is invalid!", changed == Poll.SENSOR2_MASK);
					oldValue = newValue;
				} catch (InterruptedException ie) {
					Test.assert("Thread interrupted", false);
				}
			}
		}
	}

	public void test()
	{
		new T1().start();
		new T2().start();
	}

	public static void main(String[] args)
	{
		TestNat tester = new TestNat();
		tester.test();
	}
}
