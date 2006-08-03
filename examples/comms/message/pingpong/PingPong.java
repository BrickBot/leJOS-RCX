import java.util.Random;
import josx.platform.rcx.*;

/**
  * This is an RCX class that sends and receives messages to other RCXs
  * using the SetMessage opcode.  Load it on any number of RCXs and
  * start them, they will collaboratively cout upwards.
  * @author Juergen Stuber
  */
public class PingPong
{
    private static Random r = new Random();

    public static void main(String args[])
    {
	byte count = 1;

	while (Button.RUN.isPressed()) {
	}

	while (!Button.RUN.isPressed()) {
	    int timeout = (int)System.currentTimeMillis() + delay();
	    while (!Message.isAvailable()
		   && (int)System.currentTimeMillis() < timeout) {
		Thread.yield();
	    }
	    if (Message.isAvailable()) {
		byte message = Message.get();
		Message.clear();
		if ((byte)(message - count) >= 0) {
		    count = (byte)(message + 1);
		    if (count == 0) {
			    count++;
		    }
		}
	    }
	    try {
		Thread.sleep(delay());
	    } catch (InterruptedException e) {
	    }
	    LCD.showNumber(count & 255);
	    Message.send (count);
	}

	while (Button.RUN.isPressed()) {
	}
    }

    private static int delay() {
	int delay = 10;
	while (r.nextInt(3) != 0) {
	    delay += 40;
	}
	return delay;
    }
}


