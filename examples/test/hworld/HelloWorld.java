import tinyvm.rcx.*;

/**
 * @author <a mailto="rvbijl39<at>calvin<dot>edu">Ryan VanderBijl</a>
 */

/* This class uses char[] to display text on the LCD.
 * Strings aren't really supported by TinyVM. :-(
 * 
 * This program, after d/ling, waits for you to press the RUN
 * button, and then displays "hello", for a small delay, and then
 * displays "world". (Then a small delay, and then it returns to
 * the TinyVM OS).
 */

public class HelloWorld
{
  public static void main (String[] aArg)
  {
     LCD.clear();
     LCD.refresh();

     char[] hello = {'h', 'e', 'l', 'l', 'o'};
     char[] world = {'w', 'o', 'r', 'l', 'd'};

     TextLCD.print(hello);
     LCD.refresh();

     Time.sleep(2000);

     TextLCD.print(world);
     LCD.refresh();

     Time.sleep(2000);
  }
}
