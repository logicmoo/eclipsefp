package net.sf.eclipsefp.haskell.ui.test.editor.codeassist;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for net.sf.eclipsefp.haskell.ui.test.editor.codeassist");
		//$JUnit-BEGIN$
		suite.addTestSuite(HaskellContentAssistProcessorTest.class);
		//$JUnit-END$
		return suite;
	}

}
