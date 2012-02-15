/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  
 */
package org.jajuk.ui.helpers;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * This class handles the lazy loading of nodes. It calls the necessary methods
 * on the current tree node to ask it for it's actual children.
 * 
 * Note: This implementation is rather simple and targeted for the current use
 * in Jajuk where the expanding of the node is usually a quick (in-memory)
 * operation. This implementation is not fully usable for cases where it takes
 * some time to retrieve the data, e.g. when there are database or other remote
 * requests involved.
 */
public class LazyLoadingTreeExpander implements TreeWillExpandListener {

  /** Tree Model. */
  private DefaultTreeModel model;

  /**
   * Default constructor.
   * 
   * @param model Tree model
   */
  public LazyLoadingTreeExpander(DefaultTreeModel model) {
    this.model = model;
  }

  /* (non-Javadoc)
   * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
   */
  @Override
  public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    // Do nothing on collapse.
  }

  /**
   * Invoked whenever a node in the tree is about to be expanded.
   * 
   * If the Node is a LazyLoadingTreeNode load it's children.
   * 
   * @param event DOCUMENT_ME
   * 
   * @throws ExpandVetoException the expand veto exception
   */
  @Override
  public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
    TreePath path = event.getPath();
    Object lastPathComponent = path.getLastPathComponent();
    if (lastPathComponent instanceof LazyLoadingTreeNode) {
      LazyLoadingTreeNode lazyNode = (LazyLoadingTreeNode) lastPathComponent;

      if (!lazyNode.areChildrenLoaded()) {
        MutableTreeNode[] nodes = lazyNode.loadChildren(model);
        ((DefaultMutableTreeNode) lazyNode).setAllowsChildren(nodes != null && nodes.length > 0);
        setChildren(lazyNode, nodes);
      }
    }
  }

  /**
   * Define nodes children.
   *
   * @param lazyNode DOCUMENT_ME
   * @param nodes new nodes
   */
  private void setChildren(LazyLoadingTreeNode lazyNode, MutableTreeNode... nodes) {
    int childCount = lazyNode.getChildCount();
    if (childCount > 0) {
      for (int i = 0; i < childCount; i++) {
        model.removeNodeFromParent((MutableTreeNode) lazyNode.getChildAt(0));
      }
    }
    for (int i = 0; nodes != null && i < nodes.length; i++) {
      model.insertNodeInto(nodes[i], lazyNode, i);
    }
  }
}
