import java.lang.System;
import josx.platform.rcx.*;

/**
 * This program can be used to test each sensor and motor
 * independently.<p>
 * Button functionality:
 * <ul>
 * <li> VIEW:<br>
 *   Select a port.
 * <li> PRGM:<br>
 *   Sensor ports:
 *   select input type/mode: touch/raw (0), touch/bool (1), touch/edge (2),
 *     touch/pulse (3), light/pct (4), rot/angle (5),
 *     temp/degc (6), temp/degf (7)<br>
 *   Motor ports : select power.
 * <li> RUN:<br>
 *   Sensor ports: passive (0), active (1).<br>
 *   Motor ports : float (0), forward (1), backward (2), brake (3).<br>
 *   Press longer than 0.5s to terminate the program.
 * </ul>
 * */

public class View
{
    static final int QUIT_DELAY = 500 /* ms */;

    static final PortView[] VIEWS =
	new PortView[] { SensorView.S1, SensorView.S2, SensorView.S3,
			 MotorView.A, MotorView.B, MotorView.C,
			 new BatteryView()};

    static int iCurrentView;
  
    static void viewPressed()
    {
	iCurrentView++;
	if (iCurrentView >= VIEWS.length)
	    iCurrentView = 0;
    }

    static void prgmPressed()
    {
	VIEWS[iCurrentView].prgmPressed();
    }

    static void runPressed()
    {
	VIEWS[iCurrentView].runPressed();
    }

    static void show()
    {
	LCD.clear();
	for( int i=0; i<VIEWS.length; i++){
	    if( i == iCurrentView){
		VIEWS[i].showCursor();
		VIEWS[i].showValues();
	    }
	    VIEWS[i].showPort();
	}
	LCD.refresh();
    }

    public static void main (String[] arg)
    {
	boolean quit = false;
	Poll poller = new Poll();

	iCurrentView = 0;
	show();
	new Monitor().start();
	long t = 0;

	while( !quit)
	{
	    int changed = 0;

	    try {
		changed = poller.poll(Poll.ALL_BUTTONS, 0);
	    } catch (InterruptedException ie) {
	    }

	    if ((changed & Poll.VIEW_MASK) != 0 && !Button.VIEW.isPressed())
		viewPressed();
	    if ((changed & Poll.PRGM_MASK) != 0 && !Button.PRGM.isPressed())
		prgmPressed();
	    if ((changed & Poll.RUN_MASK) != 0)
	    {
		if (Button.RUN.isPressed())
		    t = System.currentTimeMillis();
		else
		{
		    if( (int)System.currentTimeMillis()-(int)t > QUIT_DELAY)
			quit = true;
		    else
			runPressed();
		}
	    }

	    show();
	}
	for( int i=0; i<VIEWS.length; i++){
	    VIEWS[i].shutdown();
	}
    }

    static class Monitor extends Thread {
	public void run() {
	    setDaemon(true);
	    Poll poller = new Poll();
	    while (true)
	    {
		try {
		    poller.poll(Poll.ALL_SENSORS, 0);
		    show();
		} catch (InterruptedException ie) {
		}
	    }
	}
    }
}
