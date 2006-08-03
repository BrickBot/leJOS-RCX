import java.io.*;
import josx.platform.rcx.*;

/**
  * This is an RCX class that sends messages to other RCXs.
  * @author Juergen Stuber
  */
public class Sender {
  
    public static void main(String args[])
    {
	byte message = 1;

	while (Button.RUN.isPressed()) {
	}

	while (!Button.RUN.isPressed()) {
	    LCD.showNumber(message & 255);
	    Message.send (message);
	    if (message < 255) {
		message++;
	    } else {
		message = 1;
	    }
	    try {
		Thread.sleep(200);
	    } catch (InterruptedException e) {
	    }
	}

	while (Button.RUN.isPressed()) {
	}
    }
}


