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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
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

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 *  DND handler thanks  Denis Cau and its post on Java Forum : http://forum.java.sun.com/thread.jsp?forum=57&thread=296255
 * @author     Bertrand Florat
 * @created    13 feb. 2004
 */
public class TreeTransferHandler implements DragGestureListener, DragSourceListener, DropTargetListener,TreeWillExpandListener {
	
	private JTree tree;
	private DragSource dragSource; // dragsource
	private DropTarget dropTarget; //droptarget
	private static DefaultMutableTreeNode draggedNode; 
	private DefaultMutableTreeNode draggedNodeParent; 
	private static BufferedImage image = null; //buff image
	private Rectangle rect2D = new Rectangle();
	private boolean drawImage;
	
	public TreeTransferHandler(JTree tree, int action, boolean drawIcon) {
		this.tree = tree;
		tree.addTreeWillExpandListener(this);
		drawImage = drawIcon;
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, action, this);
		dropTarget = new DropTarget(tree, action, this);
	}
	
	/* Methods for DragSourceListener */
	public void dragDropEnd(DragSourceDropEvent dsde) {
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
		TreePath path = tree.getSelectionPath(); 
		if (path != null) { 
			draggedNode = (DefaultMutableTreeNode)path.getLastPathComponent();
			draggedNodeParent = (DefaultMutableTreeNode)draggedNode.getParent();
			if (drawImage) {
				Rectangle pathBounds = tree.getPathBounds(path); //getpathbounds of selectionpath
				JComponent lbl = (JComponent)tree.getCellRenderer().getTreeCellRendererComponent(tree, draggedNode, false , tree.isExpanded(path),((DefaultTreeModel)tree.getModel()).isLeaf(path.getLastPathComponent()), 0,false);//returning the label
				lbl.setBounds(pathBounds);//setting bounds to lbl
				image = new BufferedImage(lbl.getWidth(), lbl.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);//buffered image reference passing the label's ht and width
				Graphics2D graphics = image.createGraphics();//creating the graphics for buffered image
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));	//Sets the Composite for the Graphics2D context
				lbl.setOpaque(false);
				lbl.paint(graphics); //painting the graphics to label
				graphics.dispose();				
			}
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop , image, new Point(0,0),(TransferableTreeNode)draggedNode, this);			
		}	 
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
	    clearImage();
	}
	
	private final void paintImage(Point pt) {
		tree.paintImmediately(rect2D.getBounds());
		rect2D.setRect((int) pt.getX(),(int) pt.getY(),image.getWidth(),image.getHeight());
		tree.getGraphics().drawImage(image,(int) pt.getX(),(int) pt.getY(),tree);
	}
	
	private final void clearImage() {
		tree.paintImmediately(rect2D.getBounds());
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
	 */
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
	 */
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
	}
	

}
