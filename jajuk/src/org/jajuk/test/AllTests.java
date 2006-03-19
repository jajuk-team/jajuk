package org.jajuk.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.jajuk.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(TransitionDigitalDJTest.class);
		suite.addTestSuite(ProportionDigitalDJTest.class);
		//$JUnit-END$
		return suite;
	}

}
