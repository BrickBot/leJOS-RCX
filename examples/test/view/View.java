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
                     MotorView.A, MotorView.B, MotorView.C };

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

    iCurrentView = 0;
    show();
    while( !quit)
    {
      for (int i = 0; i < 3; i++)
      {
        Button b = Button.BUTTONS[i];
        if (b.isPressed())
        {
          long t = System.currentTimeMillis();
          while (b.isPressed()) { }

          if (b == Button.VIEW)
            viewPressed();
          else if (b == Button.PRGM)
            prgmPressed();
          else if (b == Button.RUN)
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
}
