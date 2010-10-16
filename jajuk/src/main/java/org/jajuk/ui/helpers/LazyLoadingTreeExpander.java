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
  /** Tree Model */
  private DefaultTreeModel model;

  /**
   * Default constructor
   * 
   * @param model
   *          Tree model
   */
  public LazyLoadingTreeExpander(DefaultTreeModel model) {
    this.model = model;
  }

  @Override
  public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    // Do nothing on collapse.
  }

  /**
   * Invoked whenever a node in the tree is about to be expanded.
   * 
   * If the Node is a LazyLoadingTreeNode load it's children.
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
   * Define nodes children
   * 
   * @param nodes
   *          new nodes
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
