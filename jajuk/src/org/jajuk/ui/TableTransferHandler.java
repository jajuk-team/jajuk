/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision$
 */

package org.jajuk.ui;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;

import javax.swing.JTable;

import org.jajuk.base.FileManager;
import org.jajuk.base.TrackManager;

import com.sun.TableSorter;

/**
 *  DND handler for table
 * @author     Bertrand Florat
 * @created    13 feb. 2004
 */
 
 public class TableTransferHandler implements DragGestureListener, DragSourceListener {
	
	private JTable jtable;
	private DragSource dragSource; // dragsource
	private DropTarget dropTarget; //droptarget
	
	public TableTransferHandler(JTable jtable, int action) {
		this.jtable = jtable;
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(jtable, action, this);
	}
	
	/* Methods for DragSourceListener */
	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess() && dsde.getDropAction()==DnDConstants.ACTION_MOVE ) {
		}
	}
	
	public final void dragEnter(DragSourceDragEvent dsde)  {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY)  {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} 
		else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} 
			else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}
	
	public final void dragOver(DragSourceDragEvent dsde) {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} 
		else  {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} 
			else  {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}
	
	public final void dropActionChanged(DragSourceDragEvent dsde)  {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		}
		else  {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} 
			else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}
	
	public final void dragExit(DragSourceEvent dse) {
		dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}	
	
	/* Methods for DragGestureListener */
	public final void dragGestureRecognized(DragGestureEvent dge) {
        TableSorter ts = ((JajukTable)jtable).getSortingModel();
        //try to find a track for this id
	    Object o = TrackManager.getTrack(ts.getValueAt(jtable.getSelectedRow(),jtable.getColumnCount()).toString());
		if ( o  == null){ //no? try to find a file for this id
			o = FileManager.getFile(ts.getValueAt(jtable.getSelectedRow(),jtable.getColumnCount()).toString());
		}
		if ( o != null){
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop ,new TransferableTableRow(o), this);
		}
	}
	
	/* Methods for DropTargetListener */
	
	public final void dragEnter(DropTargetDragEvent dtde) {
		int action = dtde.getDropAction();
		dtde.acceptDrag(action);			
	}
	
	public final void dragOver(DropTargetDragEvent dtde) {
		int action = dtde.getDropAction();
		dtde.acceptDrag(action);			
	}
	
}
