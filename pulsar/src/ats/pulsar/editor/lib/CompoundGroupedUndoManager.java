package ats.pulsar.editor.lib;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

public class CompoundGroupedUndoManager extends GroupedUndoManager {
	private static final boolean DEBUG_ADD_EDIT = false;
	private static final boolean DEBUG_SUSPENDED = false;
	protected transient boolean suspended = false;
	public CompoundGroupedUndoManager() {
		startGroup();
	}
	public synchronized void setSuspended(boolean suspended) {
		if ( DEBUG_SUSPENDED )
			if ( suspended ) {
				System.err.println();
				System.err.println("SUSPEND");
			} else {
				System.err.println("UNSUSPEND");
				System.err.println();
			}
		this.suspended = suspended;
	}
	public boolean isSuspended() {
		return suspended;
	}
	
	
	
	protected transient CompoundEdit compoundEdit = null;
	protected transient boolean requestNewCompoundEdit = true;
	public synchronized void startGroup() {
		if ( DEBUG_SUSPENDED )
			if ( suspended  ) {
				System.err.println("startGroup() ... but suspended");
			} else {
				System.err.println();
				System.err.println("startGroup()");
			}

		if ( ! suspended ) {
			this.requestNewCompoundEdit = true;
		}
	}
	
	@Override
	public synchronized void redo() throws CannotRedoException {
		try {
			setSuspended(true);
			super.redo();
		} finally {
			setSuspended(false);
			requestNewCompoundEdit = true;
		}
	}
	@Override
	public synchronized void undo() throws CannotRedoException {
		try {
			setSuspended(true);
			System.err.println( "editToBeUndone() :"+ editToBeUndone() );
			super.undo();
		} finally {
			setSuspended(false);
			requestNewCompoundEdit = true;
		}
	}
	
	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		if ( DEBUG_ADD_EDIT )
			System.err.println( anEdit.getClass().getName() + ":" +  anEdit );

		if ( this.requestNewCompoundEdit || this.compoundEdit == null ) {
			if ( compoundEdit != null )
				compoundEdit.end();
			
			this.compoundEdit =  new CompoundEdit() {
				@Override
				public boolean isInProgress() {
					return false;
				}
//				@Override
//				public boolean isSignificant() {
//					return true;
//				}
//				{
//					this.end();
//				}
			};
			super.addEdit( compoundEdit );
			this.requestNewCompoundEdit = false;
		}
		
		return this.compoundEdit.addEdit( anEdit) ;
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		if ( DEBUG_ADD_EDIT )
			System.err.println( e.getEdit().getClass().getName() + ":" +  e.getEdit() );
		
		super.undoableEditHappened(e);
	}
	
}
