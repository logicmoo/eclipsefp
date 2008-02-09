package net.sf.eclipsefp.haskell.core.test;

import net.sf.eclipsefp.haskell.core.test.compiler.CompilerManager_Test;
import net.sf.eclipsefp.haskell.core.test.compiler.CompilerOutputItem_Test;
import net.sf.eclipsefp.haskell.core.test.compiler.ListenableCompilerDecorator_Test;
import net.sf.eclipsefp.haskell.core.test.halamo.LanguageModel_Test;
import net.sf.eclipsefp.haskell.core.test.halamo.Scope_Test;
import net.sf.eclipsefp.haskell.core.test.internal.util.MultiplexedWriter_Test;
import net.sf.eclipsefp.haskell.core.test.internal.util.ResourceUtil_Test;
import net.sf.eclipsefp.haskell.core.test.project.HaskellResource_Test;
import net.sf.eclipsefp.haskell.core.test.project.ImportLibrariesList_Test;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests_Suite {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite( CompilerManager_Test.class );
		suite.addTestSuite( CompilerOutputItem_Test.class );
		suite.addTestSuite( ListenableCompilerDecorator_Test.class );
		suite.addTestSuite( ResourceUtil_Test.class );
		suite.addTestSuite( MultiplexedWriter_Test.class );
		suite.addTestSuite( LanguageModel_Test.class );
		suite.addTestSuite( Scope_Test.class );
		suite.addTestSuite( ImportLibrariesList_Test.class );
		suite.addTestSuite( HaskellResource_Test.class );
		return suite;
	}

}