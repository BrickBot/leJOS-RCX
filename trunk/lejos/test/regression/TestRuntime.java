import josx.platform.rcx.MinLCD;

public class TestRuntime {
	public static void main(String[] args) {
		Runtime rt1 = System.getRuntime();
		MinLCD.setNumber(0x301f, 100 * (int)rt1.freeMemory() / (int)rt1.totalMemory(), 0x3002);
		MinLCD.refresh();
		try {
			Thread.sleep(2000); 
		} catch (InterruptedException ie) {
		}
		Runtime rt2 = Runtime.getRuntime();
	MinLCD.setNumber(0x301f, 100 * (int)rt2.freeMemory() / (int)rt2.totalMemory(), 0x3002);
	MinLCD.refresh();
	try {
		Thread.sleep(2000); 
	} catch (InterruptedException ie) {
	}
	MinLCD.setNumber(0x301f, (int)rt2.freeMemory(), 0x3002);
	MinLCD.refresh();
	try {
		Thread.sleep(2000); 
	} catch (InterruptedException ie) {
	}
	MinLCD.setNumber(0x301f, (int)rt2.totalMemory(), 0x3002);
	MinLCD.refresh();
	try {
		Thread.sleep(2000); 
	} catch (InterruptedException ie) {
	}
	}
}