import josx.util.*;
import josx.platform.rcx.*;

/**
 * Runs first. Grabs monitor.
 */
class Min extends Thread
{
        public Min ()
        {
                setDaemon(true);
                setPriority(MIN_PRIORITY);
        }
        
        public void run()
        {
                synchronized(PriorityInversion.monitor)
                {
                        // If an inversion avoidance algorithm is running, we
                        // should eventually wake up. Otherwise, we won't!
                        try { sleep(1000); } catch (InterruptedException ie) {}
                }
        }
}

/**
 * Runs second. Blocks on monitor.
 */
class Two extends Thread
{
        public Two ()
        {
                setDaemon(true);
                setPriority(MIN_PRIORITY+1);
        }
        
        public void run()
        {
                synchronized(PriorityInversion.monitor)
                {
                }
        }
}

/**
 * Runs third. Blocks on monitor.
 */
class Max extends Thread
{
        Max ()
        {
                setDaemon(true);
                setPriority(MAX_PRIORITY);
        }
        
        public void run()
        {
                synchronized(PriorityInversion.monitor)
                {
                        // If we ever get the monitor, stop medium from running.
                        Medium.running = false;
                }
        }
}

/**
 * Runs third. If there is no prority inversion avoidance running this
 * will execute in preference to min, which as a consequence
 * never exits, which means max stays blocked. This is a problem
 * because max ought to run in preference to medium.
 */
class Medium extends Thread
{
        static boolean running = true;
        
        Medium ()
        {
                setDaemon(true);
                setPriority(NORM_PRIORITY);
        }
        
        public void run()
        {
                // Loop until max tells us to stop.
                while (running)
                {
                }
        }
}

public class PriorityInversion
{
        static Object monitor = new Object();
        
        public static void main(String args[])
                throws InterruptedException
        {
                // Set our priority to MAX
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                
                // Create min priority which grabs monitor
                new Min().start();
                
                // Let min execute.
                Thread.sleep(100);

                // Create min+1 priority which blocks on monitor
                new Two().start();
                
                // Let two execute.
                Thread.sleep(100);

                // Max should block on monitor                
                new Max().start();

                // If no inversion avoidance algorithm is running, medium
                // will block max from ever executing (because it always
                // executes in preference to min and two).                 
                new Medium().start();

                // Give the test a chance to run.                
                try { Thread.sleep(3000); } catch (Exception e) {}
                
                // If medium is still running, we've failed
                Assertion.test("Medium is still running!", !Medium.running);
        }
}
