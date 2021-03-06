/**
 * Copyright (c) 2012 by JP Moresmau
 * This code is made available under the terms of the Eclipse Public License,
 * version 1.0 (EPL). See http://www.eclipse.org/legal/epl-v10.html
 */
package net.sf.eclipsefp.haskell.ui.handlers;

import net.sf.eclipsefp.haskell.debug.core.internal.debug.HaskellDebugElement;
import net.sf.eclipsefp.haskell.debug.core.internal.debug.HaskellValue;
import net.sf.eclipsefp.haskell.ui.HaskellUIPlugin;
import net.sf.eclipsefp.haskell.ui.internal.editors.haskell.HaskellEditor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * Inspect the current value of the selected expression
 * @author JP Moresmau
 *
 */
public class DisplayExpressionHandler extends WatchExpressionHandler {

  /* (non-Javadoc)
   * @see net.sf.eclipsefp.haskell.ui.handlers.WatchExpressionHandler#addExpression(java.lang.String)
   */
  @Override
  protected void addExpression(final HaskellEditor hEditor, final String s ) {
    IAdaptable context = DebugUITools.getDebugContext();
    if (context instanceof HaskellDebugElement){
      final HaskellDebugElement hde=(HaskellDebugElement)context;
      if ((hde instanceof ISuspendResume &&  ((ISuspendResume)hde).isSuspended()) || hde.getDebugTarget().isSuspended()){
         try {
          final HaskellValue val=hde.getDebugTarget().evaluate( s,true );
          hEditor.getEditorSite().getShell().getDisplay().asyncExec( new Runnable(){
            @Override
            public void run() {
              Point p=hEditor.getSelectedPoint();
              new InspectDialog(hEditor, new Point( p.x, p.y+20 ),s,val.getValueString()).open();
              //new InspectPopupDialog( hEditor.getEditorSite().getShell(), new Point( p.x, p.y+20 ), null, new HaskellExpression( hde.getDebugTarget(), s, val ) ).open();
            }
          });

        } catch (DebugException de){
          HaskellUIPlugin.log( de );
        }

      }
    }

  }

  private static class InspectDialog extends DebugPopup{
    private final String val;
    /**
     *
     */
    public InspectDialog(final HaskellEditor hEditor,final Point location,final String exp,final String val) {
      super(hEditor.getEditorSite().getShell(),location,null);
      setTitleText( exp );
      this.val=val;
    }

     /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea( final Composite parent ) {
      Composite composite=new Composite(parent,SWT.NONE);
      composite.setLayout( new GridLayout(1,true)  );
      composite.setLayoutData( new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH) );
          //Composite)super.createDialogArea( parent );
      Label l=new Label(composite,SWT.NONE);
      l.setLayoutData( new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL) );
      l.setText( val );
      l.setLayoutData( new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH) );
      composite.layout( true );
      return composite;
    }
  }
}
