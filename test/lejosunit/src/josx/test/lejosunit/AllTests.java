package josx.test.lejosunit;

import josx.test.lejosunit.platform.rcx.AllRCXTests;
import lejosunit.framework.Test;
import lejosunit.framework.TestSuite;

/**
 * Root test suite for leJOS platform tests
 * The leJOSUnit framework (developed by Jochen Hiller) is used
 * @author <a href="mailto:mp.scholz@t-online.de">Matthias Paul Scholz</a>
 */

public class AllTests {

    /**
     * Main program to start the test suite
     */ 
    public static void main(String[] args) {
        lejosunit.rcxui.TestRunner.main(suite());
    }

    /**
     * Suite method to get all tests to run.
     * 
     * @return the whole test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(AllRCXTests.suite());
        return suite;
    }
}
