// Copyright (c) 2004-2008 by Leif Frenzel - see http://leiffrenzel.de
// This code is made available under the terms of the Eclipse Public License,
// version 1.0 (EPL). See http://www.eclipse.org/legal/epl-v10.html
package net.sf.eclipsefp.haskell.ui.internal.editors.haskell;

import java.util.Iterator;
import net.sf.eclipsefp.haskell.scion.client.IScionInstance;
import net.sf.eclipsefp.haskell.scion.client.ScionInstanceFactory;
import net.sf.eclipsefp.haskell.scion.types.Location;
import net.sf.eclipsefp.haskell.ui.HaskellUIPlugin;
import net.sf.eclipsefp.haskell.ui.internal.util.UITexts;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

class HaskellTextHover extends DefaultTextHover {

  private final HaskellEditor editor;

  private static final String ERROR_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.error"; //$NON-NLS-1$
  private static final String WARNING_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.warning"; //$NON-NLS-1$

  private final DefaultMarkerAnnotationAccess fMarkerAnnotationAccess;

  HaskellTextHover( final HaskellEditor editor,
                    final ISourceViewer sourceViewer ) {
    super( sourceViewer );
    this.editor = editor;
    fMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
  }

  @Override
  public String getHoverInfo( final ITextViewer textViewer,
                              final IRegion hoverRegion ) {
    // TODO TtC this method does not get called when hovering over a string literal
    // which leads to potential problem annotation hovers not appearing
	  String hoverInfo = computeProblemInfo( textViewer, hoverRegion );
	  if (hoverInfo != null) {
	    return hoverInfo;
	  }
	  return computeThingAtPoint( textViewer, hoverRegion );
  }

  @SuppressWarnings ( "unchecked" )
  private String computeProblemInfo( final ITextViewer textViewer, final IRegion hoverRegion ) {
    if (textViewer instanceof ISourceViewer) {
      IAnnotationModel annotationModel = ((ISourceViewer)textViewer).getAnnotationModel();
      Iterator<Annotation> i = annotationModel.getAnnotationIterator();
      while (i.hasNext()) {
        Annotation a = i.next();
        String type = a.getType();
        if (fMarkerAnnotationAccess.isSubtype( type, ERROR_ANNOTATION_TYPE ) ||
            fMarkerAnnotationAccess.isSubtype( type, WARNING_ANNOTATION_TYPE )) {
          Position p = annotationModel.getPosition( a );
          if (p.overlapsWith( hoverRegion.getOffset(), hoverRegion.getLength() )) {
            return a.getText();
          }
        }
      }
    }
    return null;
  }

  private String computeThingAtPoint( final ITextViewer textViewer, final IRegion hoverRegion  ) {
    IFile file = editor.findFile();
    if (file != null) {
      IDocument document = textViewer.getDocument();
      Location location;
      try {
        location = new Location(file.getLocation().toOSString(), document, hoverRegion);
      } catch (BadLocationException ex) {
        HaskellUIPlugin.log( UITexts.editor_textHover_error, ex );
        return null;
      }
      IScionInstance scionInstance = ScionInstanceFactory.getFactory().getScionInstance( file );
      if (scionInstance != null) {
        String thing = scionInstance.thingAtPoint(location);
        return thing; // might be null
      }
    }
    return null;
  }

  @Override
  protected boolean isIncluded( final Annotation annotation ) {
    return false;
  }

  /*
  private IFile getCabalFile( final IProject project ) {
    String ext = ResourceUtil.EXTENSION_CABAL;
    IPath path = new Path( project.getName() ).addFileExtension( ext );
    return project.getFile( path );
  }
  */
}