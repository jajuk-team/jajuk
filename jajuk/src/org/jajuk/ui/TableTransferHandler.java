/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import java.awt.Point;
import java.awt.Rectangle;
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
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *  Type description
 *
 * @author     bflorat
 * @created    13 févr. 2004
 */
/**
 * 
 *  DND handler for table
 *
 * @author     bflorat
 * @created    13 févr. 2004
 */
public class TableTransferHandler implements DragGestureListener, DragSourceListener, DropTargetListener {
	
	private JTable jtable;
	private DragSource dragSource; // dragsource
	private DropTarget dropTarget; //droptarget
	private static BufferedImage image = null; //buff image
	private Rectangle rect2D = new Rectangle();
	private boolean drawImage;
	
	public TableTransferHandler(JTable jtable, int action, boolean drawIcon) {
		this.jtable = jtable;
		drawImage = drawIcon;
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(jtable, action, this);
		dropTarget = new DropTarget(jtable, action, this);
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
	/*	Object[][] oValues = (Object[][])((TracksTableModel)jtable.getModel()).getData();
		Object o = oValuesjtable.getSelectedRow();
		dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop , image, new Point(0,0), new TransferableTableRow(((JajukTable)draggedNode).getData()), this);*/			
	}
	
	/* Methods for DropTargetListener */
	
	public final void dragEnter(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		dtde.acceptDrag(action);			
	}
	
	public final void dragExit(DropTargetEvent dte) {
		if (drawImage) {
			clearImage();
		}
	}
	
	public final void dragOver(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		dtde.acceptDrag(action);			
	}
	
	public final void dropActionChanged(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		dtde.acceptDrag(action);			
	}
	
	public final void drop(DropTargetDropEvent dtde) {
	}
	
	private final void paintImage(Point pt) {
	/*	tree.paintImmediately(rect2D.getBounds());
		rect2D.setRect((int) pt.getX(),(int) pt.getY(),image.getWidth(),image.getHeight());
		tree.getGraphics().drawImage(image,(int) pt.getX(),(int) pt.getY(),tree);*/
	}
	
	private final void clearImage() {
	//	tree.paintImmediately(rect2D.getBounds());
	}
	
	
	
	public boolean executeDrop(JTree target, DefaultMutableTreeNode draggedNode, DefaultMutableTreeNode newParentNode, int action) { 
		if (action == DnDConstants.ACTION_MOVE) {
			draggedNode.removeFromParent();
			((DefaultTreeModel)target.getModel()).insertNodeInto(draggedNode,newParentNode,newParentNode.getChildCount());
			TreePath treePath = new TreePath(draggedNode.getPath());
			target.scrollPathToVisible(treePath);
			target.setSelectionPath(treePath);
			return(true);
		}
		return(false);
	}
}
