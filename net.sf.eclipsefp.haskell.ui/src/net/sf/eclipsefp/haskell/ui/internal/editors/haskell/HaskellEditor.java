// Copyright (c) 2003-2008 by Leif Frenzel - see http://leiffrenzel.de
// This code is made available under the terms of the Eclipse Public License,
// version 1.0 (EPL). See http://www.eclipse.org/legal/epl-v10.html
package net.sf.eclipsefp.haskell.ui.internal.editors.haskell;

import java.util.ResourceBundle;

import net.sf.eclipsefp.haskell.core.halamo.IHaskellLanguageElement;
import net.sf.eclipsefp.haskell.core.halamo.ISourceLocation;
import net.sf.eclipsefp.haskell.scion.client.Scion;
import net.sf.eclipsefp.haskell.scion.commands.BackgroundTypecheckFileCommand;
import net.sf.eclipsefp.haskell.scion.commands.IScionCommandFinishedListener;
import net.sf.eclipsefp.haskell.scion.commands.LoadCommand;
import net.sf.eclipsefp.haskell.scion.commands.ScionCommand;
import net.sf.eclipsefp.haskell.scion.types.CompilationResult;
import net.sf.eclipsefp.haskell.scion.types.Note;
import net.sf.eclipsefp.haskell.ui.HaskellUIPlugin;
import net.sf.eclipsefp.haskell.ui.internal.editors.haskell.text.HaskellCharacterPairMatcher;
import net.sf.eclipsefp.haskell.ui.internal.editors.text.MarkOccurrenceComputer;
import net.sf.eclipsefp.haskell.ui.internal.preferences.editor.IEditorPreferenceNames;
import net.sf.eclipsefp.haskell.ui.internal.views.outline.HaskellOutlinePage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/** <p>The main editor class for the Haskell editor.</p>
  *
  * @author Leif Frenzel
  */
public class HaskellEditor extends TextEditor
                           implements IEditorPreferenceNames {


  /** <p>the id under which the Haskell editor is declared.</p> */
  public static final String ID = HaskellEditor.class.getName();

  /** The key binding context active while the Haskell editor is active */
  private static final String CONTEXT_ID = "net.sf.eclipsefp.haskell.ui.internal.editor.haskell.HaskellEditor.context";

  private HaskellOutlinePage outlinePage;
  private ProjectionSupport projectionSupport;
  private MarkOccurrenceComputer markOccurrencesComputer;

  public void reveal( final IHaskellLanguageElement element ) {
    Assert.isNotNull( element );
    IDocument doc = getSourceViewer().getDocument();
    ISourceLocation srcLoc = element.getSourceLocation();
    int offset = -1;
    try {
      offset = doc.getLineOffset( srcLoc.getLine() ) + srcLoc.getColumn();
    } catch( final BadLocationException badlox ) {
      // ignore
    }
    int length = element.getName().length();
    getSourceViewer().revealRange( offset, length );
  }

  public IDocument getDocument() {
    return getSourceViewer() == null ? null : getSourceViewer().getDocument();
  }


  // interface methods of TextEditor
  //////////////////////////////////

  @Override
  protected void initializeEditor() {
    super.initializeEditor();
    setSourceViewerConfiguration( new HaskellConfiguration( this ) );
    setEditorContextMenuId( "#HaskellEditorContext" );
    // we configure the preferences ourselves
    setPreferenceStore( HaskellUIPlugin.getDefault().getPreferenceStore() );
    initMarkOccurrences();
  }

  @Override
  protected boolean affectsTextPresentation( final PropertyChangeEvent evt ) {
    String prop = evt.getProperty();
    return super.affectsTextPresentation( evt ) || isAffectingProperty( prop );
  }

  @Override
  protected void configureSourceViewerDecorationSupport(
      final SourceViewerDecorationSupport support ) {
    super.configureSourceViewerDecorationSupport( support );
    support.setCharacterPairMatcher( new HaskellCharacterPairMatcher() );
    String bracketsKey = EDITOR_MATCHING_BRACKETS;
    String colorKey = EDITOR_MATCHING_BRACKETS_COLOR;
    support.setMatchingCharacterPainterPreferenceKeys( bracketsKey, colorKey );
    support.setSymbolicFontName( getFontPropertyPreferenceKey() );
  }

  @Override
  public void editorContextMenuAboutToShow( final IMenuManager menu ) {
    super.editorContextMenuAboutToShow( menu );
    if( isEditable() ) {
      IMenuManager mmSource = new MenuManager( "Source", "source" );
      menu.prependToGroup( ITextEditorActionConstants.GROUP_EDIT, mmSource );
      mmSource.add(  new Separator( "comments" ) );
      mmSource.add(  new Separator( "formatting" ) );
      mmSource.add(  new Separator( "organize" ) );
      addAction( mmSource, "comments", "Comment" );
      addAction( mmSource, "comments", "Uncomment" );
    }
    // TODO
    // menu.prependToGroup(ITextEditorActionConstants.GROUP_OPEN, openDefinitionAction);
  }

  @Override
  protected void createActions() {
    super.createActions();

    // content assist
    String defId = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
    createTextOpAction( "ContentAssistProposal",
                        ISourceViewer.CONTENTASSIST_PROPOSALS,
                        defId );

    // comment/uncomment
    createTextOpAction( "Comment",
                        ITextOperationTarget.PREFIX,
                        IActionDefinitionIds.COMMENT );
    createTextOpAction( "Uncomment",
                        ITextOperationTarget.STRIP_PREFIX,
                        IActionDefinitionIds.UNCOMMENT );

    // "open definition" action TODO remove
//    openDefinitionAction = new OpenDefinitionHandler(this);
//	openDefinitionAction.setActionDefinitionId(IActionDefinitionIds.OPEN_DEFINITION);
//	setAction("OpenDefinition", openDefinitionAction);
  }

  @Override
  protected ISourceViewer createSourceViewer( final Composite parent,
                                              final IVerticalRuler ruler,
                                              final int styles ) {
    // copied this from the super class, replaced source viewer with
    // projection viewer
    fAnnotationAccess = createAnnotationAccess();
    fOverviewRuler = createOverviewRuler( getSharedColors() );
    ISourceViewer viewer = new ProjectionViewer( parent,
                                                 ruler,
                                                 getOverviewRuler(),
                                                 isOverviewRulerVisible(),
                                                 styles);
    // ensure decoration support has been created and configured.
    getSourceViewerDecorationSupport(viewer);
    return viewer;
  }

  @Override
  public void createPartControl( final Composite parent ) {
    super.createPartControl( parent );
    ProjectionViewer projectionViewer = ( ProjectionViewer )getSourceViewer();
    projectionSupport = new ProjectionSupport( projectionViewer,
                                               getAnnotationAccess(),
                                               getSharedColors() );
    projectionSupport.install();
    projectionViewer.doOperation( ProjectionViewer.TOGGLE );

    if( markOccurrencesComputer != null ) {
      ISelectionChangedListener listener = new ISelectionChangedListener() {
        public void selectionChanged( final SelectionChangedEvent event ) {
          IDocument doc = getSourceViewer().getDocument();
          markOccurrencesComputer.setDocument( doc );
          markOccurrencesComputer.compute();
        }
      };
      projectionViewer.addPostSelectionChangedListener( listener );
    }
    activateContext();
  }

  private void activateContext() {
	IContextService contextService = (IContextService)getSite().getService(IContextService.class);
	contextService.activateContext(CONTEXT_ID);
  }

  @Override
  public Object getAdapter( final Class required ) {
    Object result = null;
    // adapt the displayed source file to the outline viewer
    if( IContentOutlinePage.class.equals( required ) ) {
      if( outlinePage == null ) {
        outlinePage = new HaskellOutlinePage( this );
        if( getEditorInput() != null ) {
          outlinePage.setInput( getEditorInput() );
        }
      }
      result = outlinePage;
    } else if ( projectionSupport != null ) {
      result = projectionSupport.getAdapter( getSourceViewer(), required );
    }

    if( result == null ) {
      result = super.getAdapter( required );
    }
    return result;
  }

  // supplement some TextEditor funtionality with specific handling
  // needed because we have an attached outline page
  /////////////////////////////////////////////////////////////////

  @Override
  public void dispose() {
    if( outlinePage != null ) {
      outlinePage.setInput( null );
    }
    super.dispose();
  }

  @Override
  public void doRevertToSaved() {
    super.doRevertToSaved();
    if( outlinePage != null ) {
      outlinePage.update();
    }
  }

  @Override
  public void doSave( final IProgressMonitor monitor ) {
    super.doSave( monitor );
    if( outlinePage != null ) {
      outlinePage.update();
    }
  }

  @Override
  public void doSaveAs() {
    super.doSaveAs();
    if( outlinePage != null ) {
      outlinePage.update();
    }
  }

  @Override
  protected void editorSaved() {
    // Reload the file on the Scion server side
	try {
	  IFile file = findFile();
	  if (file != null) {
		String fileName = file.getLocation().toOSString();

		LoadCommand loadCommand = new LoadCommand(fileName);
		loadCommand.addFinishedListener(new IScionCommandFinishedListener(){
			public void onScionCommandFinished(final ScionCommand command) {
				if (command.isSuccessful()) {
					addMarkers(((LoadCommand)command).getCompilationResult());
				}
			}
		});
		Scion.asyncRunCommand(loadCommand);

		BackgroundTypecheckFileCommand typecheckCommand = new BackgroundTypecheckFileCommand(fileName);
		typecheckCommand.addFinishedListener(new IScionCommandFinishedListener(){
			public void onScionCommandFinished(final ScionCommand command) {
				if (command.isSuccessful()) {
					addMarkers(((BackgroundTypecheckFileCommand)command).getCompilationResult());
				}
			}
		});
		Scion.asyncRunCommand(typecheckCommand);
	  }
	} catch (Exception ex) {
	  // We should never let Scion errors prevent the file from being saved!
	  ex.printStackTrace(System.out); // TODO
	}
  }

  /**
   * TODO maybe this should be in another class?
   * Somebody, somewhere, clears the markers too...
   */
  protected void addMarkers(final CompilationResult compilationResult) {
	  IFile file = findFile();
	  if (file != null) {
		  for (Note note : compilationResult.getNotes()) {
			  try {
				  IMarker marker = file.createMarker(IMarker.PROBLEM);
				  int severity;
				  switch (note.getKind()) {
					  case ERROR: severity = IMarker.SEVERITY_ERROR; break;
					  case WARNING: severity = IMarker.SEVERITY_WARNING; break;
					  case INFO: severity = IMarker.SEVERITY_INFO; break;
					  default: severity = IMarker.SEVERITY_INFO; break;
				  }
				  marker.setAttribute(IMarker.SEVERITY, severity);
				  marker.setAttribute(IMarker.CHAR_START, note.getLocation().getStartOffset(getDocument()));
				  marker.setAttribute(IMarker.CHAR_END, note.getLocation().getEndOffset(getDocument()));
				  marker.setAttribute(IMarker.MESSAGE, note.getMessage());
			  } catch (CoreException ex) {
				  // too bad, no marker then!
			  } catch (BadLocationException ex) {
				  // too bad, no marker then!
			  }
		  }
	  }
  }

  public IFile findFile() {
    IFile result = null;
    IEditorInput input = getEditorInput();
    if( input instanceof IFileEditorInput ) {
      result = ( ( IFileEditorInput )input ).getFile();
    }
    return result;
  }

  @Override
  public void doSetInput( final IEditorInput input ) throws CoreException {
    super.doSetInput( input );
    if( outlinePage != null ) {
      outlinePage.setInput( input );
    }
  }


  // helping methods
  //////////////////

  private boolean isAffectingProperty( final String property ) {
    return    property.equals( EDITOR_COMMENT_COLOR )
           || property.equals( EDITOR_COMMENT_BOLD )
           || property.equals( EDITOR_LITERATE_COMMENT_COLOR )
           || property.equals( EDITOR_LITERATE_COMMENT_BOLD )
           || property.equals( EDITOR_DEFAULT_COLOR )
           || property.equals( EDITOR_DEFAULT_BOLD )
           || property.equals( EDITOR_FUNCTION_COLOR )
           || property.equals( EDITOR_FUNCTION_BOLD )
           || property.equals( EDITOR_KEYWORD_COLOR )
           || property.equals( EDITOR_KEYWORD_BOLD )
           || property.equals( EDITOR_STRING_COLOR )
           || property.equals( EDITOR_STRING_BOLD )
           || property.equals( EDITOR_CHAR_COLOR )
           || property.equals( EDITOR_CHAR_BOLD );
  }

  private void createTextOpAction( final String name,
                                   final int targetId,
                                   final String actionDefinitionId ) {
    ResourceBundle bundle = HaskellUIPlugin.getDefault().getResourceBundle();
    Action action = new TextOperationAction( bundle,
                                             name + ".",
                                             this,
                                             targetId );
    action.setActionDefinitionId( actionDefinitionId );
    setAction( name, action );
    markAsStateDependentAction( name, true );
  }

  private void initMarkOccurrences() {
	// TODO replace by something not Cohatoe-based
	/*
    CohatoeServer server = CohatoeServer.getInstance();
    IMarkOccurrences mo = server.createFunction( IMarkOccurrences.class );
    if( mo != null ) {
      markOccurrencesComputer = new MarkOccurrenceComputer( this, mo );
    }
    */
  }
}