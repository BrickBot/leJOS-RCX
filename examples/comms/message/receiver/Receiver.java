import java.io.*;
import josx.platform.rcx.*;

/**
  * This is an RCX class that receives messages from other RCXs.
  * @author Juergen Stuber
  */
public class Receiver {
  
    public static void main(String args[])
    {
	while (Button.RUN.isPressed()) {
	}

	while (!Button.RUN.isPressed()) {
	    while (!Message.isAvailable() && !Button.RUN.isPressed()) {
		Thread.yield();
	    }
	    LCD.showNumber(Message.get() & 255);
	}

	while (Button.RUN.isPressed()) {
	}
    }
}


