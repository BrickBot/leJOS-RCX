package josx.test.lejosunit.platform.rcx;

import josx.platform.rcx.Battery;
import lejosunit.framework.TestCase;

/**
 * Test cases for tests of the leJOS class josx.platform.rcx.Battery
 * The leJOSUnit framework (developed by Jochen Hiller) is used
 * @author <a href="mailto:mp.scholz@t-online.de">Matthias Paul Scholz</a>
 */

public class BatteryTest extends TestCase {

    /**
     * Constructor to create a test case.
     * 
     * @param name the name of the test case
     */
    public BatteryTest(String name) {
        super(name);
    }
    
    /**
     * Run a test. Do the dispatch here.
     * 
     * @throws Throwable a general error
     */
    protected void runTest() throws Throwable {
    	// run tests
    	testVoltage();
    }
    
    // test methods

    /**
     * A test of the getVoltage method
     */
    public void testVoltage() {
        assertTrue(Battery.getVoltage()>0.0);
    }    
}
