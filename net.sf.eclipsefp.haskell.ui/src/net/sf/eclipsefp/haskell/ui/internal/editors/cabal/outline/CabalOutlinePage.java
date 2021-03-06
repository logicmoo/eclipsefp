// Copyright (c) 2006 by Leif Frenzel <himself@leiffrenzel.de>
// All rights reserved.
package net.sf.eclipsefp.haskell.ui.internal.editors.cabal.outline;

import net.sf.eclipsefp.haskell.core.cabalmodel.PackageDescription;
import net.sf.eclipsefp.haskell.ui.internal.editors.cabal.CabalEditor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/** <p>content outline for the Cabal editor.</p>
  *
  * @author Leif Frenzel
  */
public class CabalOutlinePage extends ContentOutlinePage {

  private final CabalEditor editor;
  private PackageDescription packageDescription;

  public CabalOutlinePage( final CabalEditor editor,
                           final PackageDescription packageDescription ) {
    this.editor = editor;
    this.packageDescription = packageDescription;
  }

  public void setPackageDescription( final PackageDescription pd ) {
    this.packageDescription = pd;
    if( getTreeViewer() != null ) {
      getTreeViewer().setInput( pd );
    }
  }


  // interface methods of ContentOutlinePage
  //////////////////////////////////////////

  @Override
  public void createControl( final Composite parent ) {
    super.createControl( parent );
    TreeViewer tv = getTreeViewer();
    tv.setLabelProvider( new CabalOutlineLP() );
    tv.setContentProvider( new CabalOutlinePageCP() );
    tv.setAutoExpandLevel( AbstractTreeViewer.ALL_LEVELS );
    tv.addSelectionChangedListener( new ISelectionChangedListener() {
      @Override
      public void selectionChanged(final SelectionChangedEvent event) {
        if( event.getSelection() instanceof IStructuredSelection ) {
          IStructuredSelection ssel = ( IStructuredSelection )event.getSelection();
          if( ssel.size() == 1 ) {
            editor.selectAndReveal( ssel.getFirstElement() );
          }
        }
      }
    });

    tv.setInput( packageDescription );
  }
}