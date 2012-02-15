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

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

/**
 * Abstract class that is used to load TreeNodes lazily, i.e. in large trees
 * with many sub-branches this can reduce the number of nodes that are actually
 * created a lot.
 */
public abstract class LazyLoadingTreeNode extends DefaultMutableTreeNode {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new transferable tree node.
   * 
   * @param userObject an Object provided by the user that constitutes the node's data
   */
  public LazyLoadingTreeNode(Object userObject) {
    super(userObject);
    setAllowsChildren(true);
  }

  /**
   * Check if there are children loaded already.
   * 
   * @return <code>true</code> if there are some childrens
   */
  public boolean areChildrenLoaded() {
    return getChildCount() > 0 && getAllowsChildren();
  }

  /**
   * If the.
   * 
   * @return false, this node can't be a leaf
   * 
   * @see #getAllowsChildren()
   */
  @Override
  public boolean isLeaf() {
    return !getAllowsChildren();
  }

  /**
   * This is the point where the actual implementation can create the actual
   * child nodes of the current node at the point when the node is expanded.
   * 
   * This method will be executed in a background thread. If you have to do some
   * GUI stuff use {@link SwingUtilities#invokeLater(Runnable)}
   * 
   * @param model DOCUMENT_ME
   * 
   * @return The created nodes.
   */
  public abstract MutableTreeNode[] loadChildren(DefaultTreeModel model);
}
