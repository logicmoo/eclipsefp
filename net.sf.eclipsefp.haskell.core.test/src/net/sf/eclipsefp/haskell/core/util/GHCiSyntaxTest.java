package net.sf.eclipsefp.haskell.core.util;

import java.util.regex.Matcher;
import net.sf.eclipsefp.haskell.util.PlatformUtil;
import junit.framework.TestCase;

/**
 * Test GHCi syntax patterns and such
 * @author JP Moresmau
 *
 */
public class GHCiSyntaxTest extends TestCase {

  public GHCiSyntaxTest( final String name ) {
    super( name );
  }

  public void testBreakpointPattern(){
    String s="Breakpoint 0 activated at D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Main.hs:9:4-14"+PlatformUtil.NL+"*Main> ";
    Matcher m=GHCiSyntax.BREAKPOINT_SET_PATTERN.matcher( s );
    assertTrue(m.find());
    assertEquals("0",m.group( 1 ));
    assertEquals("D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Main.hs:9:4-14",m.group( 2 ));
  }

  public void testBreakpointPatternLong(){
    String s="GHCi, version 6.10.4: https://www.haskell.org/ghc/  :? for help"+PlatformUtil.NL+
    "  Loading package ghc-prim ... linking ... done."+PlatformUtil.NL+
    "  Loading package integer ... linking ... done."+PlatformUtil.NL+
    "  Loading package base ... linking ... done."+PlatformUtil.NL+
    "  Loading package syb ... linking ... done."+PlatformUtil.NL+
    "  Loading package array-0.2.0.0 ... linking ... done."+PlatformUtil.NL+
    "  Loading package containers-0.2.0.1 ... linking ... done."+PlatformUtil.NL+
    "  [1 of 3] Compiling Test             ( D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Test.hs, interpreted )"+PlatformUtil.NL+
    ""+PlatformUtil.NL+
    "  D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Test.hs:4:0:"+PlatformUtil.NL+
    "      Warning: Module `Data.Map' is imported, but nothing from it is used,"+PlatformUtil.NL+
    "                 except perhaps instances visible in `Data.Map'"+PlatformUtil.NL+
    "               To suppress this warning, use: import Data.Map()"+PlatformUtil.NL+
    ""+PlatformUtil.NL+
    "  D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Test.hs:29:0:"+PlatformUtil.NL+
    "      Warning: Pattern match(es) are overlapped"+PlatformUtil.NL+
    "               In the definition of `testMethod': testMethod \"\" = ..."+PlatformUtil.NL+
    "  [2 of 3] Compiling Module1          ( D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Module1.hs, interpreted )"+PlatformUtil.NL+
    ""+PlatformUtil.NL+
    "  D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Module1.hs:4:0:"+PlatformUtil.NL+
    "     Warning: Definition but no type signature for `testfunc1'"+PlatformUtil.NL+
    "               Inferred type: testfunc1 :: [Char]"+PlatformUtil.NL+
    ""+PlatformUtil.NL+
    "  D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Module1.hs:6:0:"+PlatformUtil.NL+
    "      Warning: Definition but no type signature for `testfunc1bis'"+PlatformUtil.NL+
    "               Inferred type: testfunc1bis :: [Char]"+PlatformUtil.NL+
    "  [3 of 3] Compiling Main             ( D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Main.hs, interpreted )"+PlatformUtil.NL+
    "  Ok, modules loaded: Main, Module1, Test."+PlatformUtil.NL+
    "  *Main> Breakpoint 0 activated at D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Main.hs:9:4-14"+PlatformUtil.NL+
    "  *Main> ";
    Matcher m=GHCiSyntax.BREAKPOINT_SET_PATTERN.matcher( s );
    assertTrue(m.find());
    assertEquals("0",m.group( 1 ));
    assertEquals("D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Main.hs:9:4-14",m.group( 2 ));

  }

  public void testBreakpointLocationPattern(){
    String s="D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Main.hs:9:4-14";
    //Pattern p=Pattern.compile( "([^\\:]+)");
    Matcher m=GHCiSyntax.BREAKPOINT_LOCATION_PATTERN.matcher( s );
    assertTrue(m.matches());
    assertEquals("D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Main.hs",m.group( 1 ));
    assertEquals("9",m.group( 2 ));
    assertEquals("4",m.group( 3 ));
    assertEquals("14",m.group( 4 ));
  }

  public void testBreakpointLocationMultilinePattern(){
    String s="D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Test.hs:(29,0)-(35,13)";
    //Pattern p=Pattern.compile( "([^\\:]+)");
    Matcher m=GHCiSyntax.BREAKPOINT_LOCATIONMULTILINE_PATTERN.matcher( s );
    assertTrue(m.matches());
    assertEquals("D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\Haskell0\\src\\Test.hs",m.group( 1 ));
    assertEquals("29",m.group( 2 ));
    assertEquals("0",m.group( 3 ));
    assertEquals("35",m.group( 4 ));
    assertEquals("13",m.group( 5 ));
  }

  public void testBindingPattern(){
    String s="_result :: IO String = _";
    Matcher m=GHCiSyntax.BINDING_PATTERN.matcher( s );
    assertTrue(m.matches());
    assertEquals("_result",m.group(1));
    assertEquals(" IO String",m.group(2));
    assertEquals("IO String",GHCiSyntax.formatType( m.group(2)));
    assertEquals("_",m.group(4));

    s="_result :: IO String";
    m=GHCiSyntax.BINDING_PATTERN.matcher( s );
    assertTrue(m.matches());
    assertEquals("_result",m.group(1));
    assertEquals(" IO String",m.group(2));
    assertEquals("IO String",GHCiSyntax.formatType( m.group(2)));
    assertNull( m.group(3));
    assertNull( m.group(4));
  }

  public void testBindingPatternMultiLine(){
    String s="soup ::"
      +PlatformUtil.NL+"  [Tag"
      +PlatformUtil.NL+   "     String] = [TagOpen \"html\" [],TagClose \"html\"]";
    Matcher m=GHCiSyntax.BINDING_PATTERN.matcher( s );
    assertTrue(m.matches());
    assertEquals("soup",m.group(1));
    assertEquals(PlatformUtil.NL+"  [Tag"
      +PlatformUtil.NL+   "     String]",m.group(2));
    assertEquals("[Tag String]",GHCiSyntax.formatType( m.group(2)));
    assertEquals("[TagOpen \"html\" [],TagClose \"html\"]",m.group(4));
  }

  public void testContextPattern(){
    String s="--> main"
      +PlatformUtil.NL+"Stopped at D:\\dev\\haskell\\jp-github\\runtime-New_configuration\\DebugP\\src\\Main.hs:16:10-21";
    Matcher m=GHCiSyntax.CONTEXT_PATTERN.matcher( s );
    assertTrue(m.find());
    assertEquals("main",m.group(1));
  }
}
