/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
 *  http://jajuk.info
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

package org.jajuk.ui.helpers;

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
 * DND handler thanks Denis Cau and its post on Java Forum :
 * http://forum.java.sun.com/thread.jsp?forum=57&thread=296255
 */
public class TreeTransferHandler implements DragGestureListener, DragSourceListener,
    DropTargetListener, TreeWillExpandListener {

  /** DOCUMENT_ME. */
  private final JTree tree;

  /** DOCUMENT_ME. */
  private final DragSource dragSource; // dragsource

  /** DOCUMENT_ME. */
  private static DefaultMutableTreeNode draggedNode;

  /** DOCUMENT_ME. */
  private static BufferedImage image = null; // buff image

  /** DOCUMENT_ME. */
  private final Rectangle rect2D = new Rectangle();

  /** DOCUMENT_ME. */
  private final boolean drawImage;

  /**
   * Instantiates a new tree transfer handler.
   * 
   * @param tree DOCUMENT_ME
   * @param action DOCUMENT_ME
   * @param drawIcon DOCUMENT_ME
   */
  public TreeTransferHandler(JTree tree, int action, boolean drawIcon) {
    this.tree = tree;
    tree.addTreeWillExpandListener(this);
    drawImage = drawIcon;
    dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer(tree, action, this);
  }

  /* Methods for DragSourceListener */
  /* (non-Javadoc)
   * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
   */
  public void dragDropEnd(DragSourceDropEvent dsde) {
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
   */
  public final void dragEnter(DragSourceDragEvent dsde) {
    int action = dsde.getDropAction();
    if (action == DnDConstants.ACTION_COPY) {
      dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
    } else {
      if (action == DnDConstants.ACTION_MOVE) {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      } else {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
      }
    }
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
   */
  public final void dragOver(DragSourceDragEvent dsde) {
    int action = dsde.getDropAction();
    if (action == DnDConstants.ACTION_COPY) {
      dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
    } else {
      if (action == DnDConstants.ACTION_MOVE) {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      } else {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
      }
    }
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
   */
  public final void dropActionChanged(DragSourceDragEvent dsde) {
    int action = dsde.getDropAction();
    if (action == DnDConstants.ACTION_COPY) {
      dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
    } else {
      if (action == DnDConstants.ACTION_MOVE) {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      } else {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
      }
    }
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
   */
  public final void dragExit(DragSourceEvent dse) {
    dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  /* Methods for DragGestureListener */
  /* (non-Javadoc)
   * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
   */
  public final void dragGestureRecognized(DragGestureEvent dge) {
    TreePath path = tree.getSelectionPath();
    if (path != null) {
      draggedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
      if (drawImage) {
        // get path bounds of selection path
        Rectangle pathBounds = tree.getPathBounds(path);
        // returning the label
        JComponent lbl = (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(tree,
            draggedNode, false, tree.isExpanded(path),
            ((DefaultTreeModel) tree.getModel()).isLeaf(path.getLastPathComponent()), 0, false);
        // setting bounds to lbl
        lbl.setBounds(pathBounds);
        // buffered image reference passing the label's ht and width
        image = new BufferedImage(lbl.getWidth(), lbl.getHeight(),
            java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
        // creating the graphics for buffered image
        Graphics2D graphics = image.createGraphics();
        // Sets the Composite for the Graphics2D context
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        lbl.paint(graphics); // painting the graphics to label
        graphics.dispose();
      }
      dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image, new Point(0, 0),
          (TransferableTreeNode) draggedNode, this);
    }
  }

  /* Methods for DropTargetListener */

  /* (non-Javadoc)
   * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
   */
  public final void dragEnter(DropTargetDragEvent dtde) {
    Point pt = dtde.getLocation();
    int action = dtde.getDropAction();
    if (drawImage) {
      paintImage(pt);
    }
    dtde.acceptDrag(action);
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
   */
  public final void dragExit(DropTargetEvent dte) {
    if (drawImage) {
      clearImage();
    }
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
   */
  public final void dragOver(DropTargetDragEvent dtde) {
    Point pt = dtde.getLocation();
    int action = dtde.getDropAction();
    if (drawImage) {
      paintImage(pt);
    }
    dtde.acceptDrag(action);
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
   */
  public final void dropActionChanged(DropTargetDragEvent dtde) {
    Point pt = dtde.getLocation();
    int action = dtde.getDropAction();
    if (drawImage) {
      paintImage(pt);
    }
    dtde.acceptDrag(action);
  }

  /* (non-Javadoc)
   * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
   */
  public final void drop(DropTargetDropEvent dtde) {
    clearImage();
  }

  /**
   * Paint image.
   * DOCUMENT_ME
   * 
   * @param pt DOCUMENT_ME
   */
  private final void paintImage(Point pt) {
    tree.paintImmediately(rect2D.getBounds());
    rect2D.setRect((int) pt.getX(), (int) pt.getY(), image.getWidth(), image.getHeight());
    tree.getGraphics().drawImage(image, (int) pt.getX(), (int) pt.getY(), tree);
  }

  /**
   * Clear image.
   * DOCUMENT_ME
   */
  private final void clearImage() {
    tree.paintImmediately(rect2D.getBounds());
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
   */
  public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
   */
  public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
    // required by interface, but nothing to do here...
  }
}
