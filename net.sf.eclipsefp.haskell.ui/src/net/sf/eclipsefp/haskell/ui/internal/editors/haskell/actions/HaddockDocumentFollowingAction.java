package net.sf.eclipsefp.haskell.ui.internal.editors.haskell.actions;

import java.util.ResourceBundle;
import net.sf.eclipsefp.haskell.ui.editor.actions.IEditorActionDefinitionIds;
import net.sf.eclipsefp.haskell.ui.internal.editors.haskell.HaskellEditor;
import net.sf.eclipsefp.haskell.ui.internal.util.UITexts;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/** Implements Haddock -> Document Following item/element */
public final class HaddockDocumentFollowingAction extends TextEditorAction {
  /** The Haddock following item documentation marker */
  private static final String FOLLOWDOC_MARKER = "-- | ";
  /** The documentation for the user to replace */
  static final String USER_REPLACES = UITexts.HaddockDocumentation_user_replaces;

  /** Default constructor */
  public HaddockDocumentFollowingAction(final ResourceBundle bundle, final String prefix, final ITextEditor editor) {
    super(bundle, prefix, editor);
    setId( HaskellEditor.HADDOCK_DOCUMENT_FOLLOWING_ACTION );
    setActionDefinitionId( IEditorActionDefinitionIds.HADDOCK_FOLLOWING );
  }

  @Override
  public void update() {
    super.update();
    if (!isEnabled()) {
      return;
    }
    if (!canModifyEditor()) {
      setEnabled( false );
      return;
    }
  }

  @Override
  public void run() {
    // Insert a new line above the current line, inserting the "-- | " code:
    ITextEditor editor = getTextEditor();
    if (editor instanceof HaskellEditor) {
      HaskellEditor hEditor = (HaskellEditor) editor;
      IDocument doc = hEditor.getDocument();

      ISelectionProvider selectionProvider = editor.getSelectionProvider();
      ISelection selection = selectionProvider.getSelection();
      if (selection instanceof ITextSelection) {
        ITextSelection textSelection = ( ITextSelection )selection;
        int offset = textSelection.getOffset();

        if (offset > -1) {
          try {
            IRegion currentLine = doc.getLineInformationOfOffset( offset );
            int startOfLineOffset = currentLine.getOffset();

            doc.replace(startOfLineOffset, 0,
                        FOLLOWDOC_MARKER.concat(USER_REPLACES).concat( TextUtilities.getDefaultLineDelimiter( doc ) ));

            ITextSelection newCursor = new TextSelection( doc, startOfLineOffset + FOLLOWDOC_MARKER.length(),
                                                          USER_REPLACES.length() );
            selectionProvider.setSelection( newCursor );
          } catch( BadLocationException ex ) {
            // Ignore and continue...
          }
        }
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}