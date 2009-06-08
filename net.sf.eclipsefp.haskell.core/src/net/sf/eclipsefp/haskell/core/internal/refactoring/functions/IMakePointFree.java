// Copyright (c) 2007-2008 by Leif Frenzel - see http://leiffrenzel.de
// This code is made available under the terms of the Eclipse Public License,
// version 1.0 (EPL). See http://www.eclipse.org/legal/epl-v10.html
package net.sf.eclipsefp.haskell.core.internal.refactoring.functions;

/** <p>interface for the Haskell function that performs the pointfree
  * refactoring.</p>
  *
  * @author Leif Frenzel
  */
public interface IMakePointFree {

  String makePointFree( String content );
}