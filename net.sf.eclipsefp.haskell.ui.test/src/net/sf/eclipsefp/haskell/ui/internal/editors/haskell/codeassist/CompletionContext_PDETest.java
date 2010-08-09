/*******************************************************************************
 * Copyright (c) 2005, 2006 Thiago Arrais and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thiago Arrais
 *******************************************************************************/
package net.sf.eclipsefp.haskell.ui.internal.editors.haskell.codeassist;


import junit.framework.TestSuite;
import net.sf.eclipsefp.haskell.core.internal.util.CompletionProposalTestCase;
import net.sf.eclipsefp.haskell.core.test.TestCaseWithProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class CompletionContext_PDETest extends TestCaseWithProject{

  public static TestSuite suite(){
    TestSuite ts=new TestSuite("CompletionContext_PDETest");
    ts.addTestSuite( CompletionContext_PDETest.class);
    //ts.addTest(new CompletionContext_PDETest("testDeclarationCompletion"));
    return ts;
  }

	public CompletionContext_PDETest() {
    super();
  }

  public CompletionContext_PDETest( final String name ) {
    super( name );
  }

  public void testDeclarationCompletion() throws CoreException {
		final String input = "module CompletionEngineTest where\n" +
				             "\n" +
				             "popStr str = str\n" +
				             "\n" +
				             "main = popStr\n";
	//	final ICompilationUnit unit = parseAsFile(input);
		HaskellCompletionContext context = createContext("CompletionEngineTest",input, 62);

		assertEquals('o', input.charAt(62 - 1));

		ICompletionProposal[] proposals = context.computeProposals();

		assertContains(createProposal("po", "popStr", 62), proposals);
	}

	public void testPreludeClassCompletion() throws CoreException {
		final String input = "module CompletionEngineTest where\n" +
                             "\n" +
                             "fat :: N";
	//	final ICompilationUnit unit = parseAsFile(input);
		HaskellCompletionContext context = createContext("CompletionEngineTest",input, 43);

		assertEquals('N', input.charAt(43 - 1));

		ICompletionProposal[] proposals = context.computeProposals();

		assertContains(createProposal("N", "Num", 43), proposals);
	}

	public void testKeywordCompletion() throws CoreException {
		final String input = "module CompletionEngineTest wh";
		//TODO avoid complaining about parsing error here
	//	final ICompilationUnit unit = parseAsFile(input);
		HaskellCompletionContext context = createContext("CompletionEngineTest",input, 30);

		assertEquals('h', input.charAt(30 - 1));

		ICompletionProposal[] proposals = context.computeProposals();

		assertContains(createProposal("wh", "where", 30), proposals);
	}

	public void testDiscoverPrefixAfterLeftParen() throws CoreException {
		final String input = "module Factorial where\n" +
				             "\n" +
				             "fat 0 = 1\n" +
				             "fat 1 = n * (f";
		//final ICompilationUnit unit = parseAsFile(input);
		HaskellCompletionContext context = createContext("Factorial",input, 48);

		assertEquals('f', input.charAt(48 - 1));

		ICompletionProposal[] proposals = context.computeProposals();

		assertContains(createProposal("f", "fat", 48), proposals);
	}

	public void testDoNotCompleteOnEmptyPrefix() throws CoreException {
		final String input = "module Factorial where\n" +
				             "\n" +
				             "fat 0 = 1\n" +
				             "fat 1 = n * (";
		//final ICompilationUnit unit = parseAsFile(input);
		HaskellCompletionContext context = createContext("Factorial",input, 48);

		assertEquals('(', input.charAt(47 - 1));

		ICompletionProposal[] proposals = context.computeProposals();

		assertEquals(0, proposals.length);
	}

	public void testCompletePrefixWithUnderscore() throws CoreException {
		final String input = "module Underscore where\n" +
				             "\n" +
				             "_underscore = '_'\n" +
				             "prefixWithUnderscore str = _und";
		//final ICompilationUnit unit = parseAsFile(input);
		final HaskellCompletionContext context = createContext("Underscore",input, 74);

		assertEquals('d', input.charAt(74 - 1));

		ICompletionProposal[] proposals = context.computeProposals();

		assertContains(createProposal("_und", "_underscore", 74), proposals);
	}

	public void testSeeAcrossModules() throws CoreException {
		final String input = "module Main where\n" +
				             "\n" +
				             "main = putStr $ show $ f";
		final int offset = input.length();
		//final ICompilationUnit unit = parseAsFile(input);
		//final StubHalamo langModel = new StubHalamo();
		HaskellCompletionContext context = createContext("Main",input, offset);

		//langModel.setModulesInScope(new StubModule("Recursive", "fat", "fib"));

		ICompletionProposal[] proposals = context.computeProposals();

		assertContains(createProposal("f", "fat", offset), proposals);
		assertEquals("fat - Recursive", proposals[0].getDisplayString());
	}

	public void testCompletesModuleNames() throws CoreException {
		final String input = "module Main where\n" +
							 "\n" +
		                     "import Fib";
		final int offset = input.length();
		//final StubHalamo langModel = new StubHalamo();
		HaskellCompletionContext context = createContext("Main",input, offset);

	//	langModel.putModule(new StubModule("Fibonacci"));

		ICompletionProposal[] proposals = context.computeProposals();

		assertContains(createProposal("Fib", "Fibonacci", offset), proposals);
	}

	//TODO do not propose an already imported module

	public void testDoNotProposeSameFunctionTwice() throws CoreException {
		final String input = "module Main where\n" +
		                     "\n" +
		                     "factorial :: Int -> Int\n" +
		                     "factorial = foldr (*) 1 . enumFromTo 1\n" +
		                     "\n" +
		                     "main = putStr $ show $ fac";
		HaskellCompletionContext context = createContext("Main",input, input.length());

		ICompletionProposal[] proposals = context.computeProposals();

		assertEquals(1, proposals.length);
	}

	private void assertContains(final ICompletionProposal proposal, final ICompletionProposal[] proposals) {
		CompletionProposalTestCase.assertContains(proposal, proposals);
	}

	private ICompletionProposal createProposal(final String replaced, final String replacement, final int offset) {
		return CompletionProposalTestCase.createProposal(replaced, replacement, offset);
	}

	private HaskellCompletionContext createContext(final String module,final String source, final int offset) {

	  try {
	    IFile f=addFile( module, source );
	    return new HaskellCompletionContext(f,source, offset);
	  } catch (Exception ce){
	    ce.printStackTrace();
	    fail( ce.getLocalizedMessage() );
	  }
	  return null;
	}



}
