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
 *  $Revision$
 */

package org.jajuk.ui.helpers;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jajuk.base.Item;

/**
 * DND handler for jtree.
 */
public class TreeTransferHandler extends TransferHandler {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  final JTree jtree;

  /**
   * Constructor.
   * 
   * @param jtree DOCUMENT_ME
   */
  public TreeTransferHandler(final JTree jtree) {
    this.jtree = jtree;
  }

  /**
   * Called when dragging.
   * 
   * @param c DOCUMENT_ME
   * 
   * @return the transferable
   */
  @SuppressWarnings("unchecked")
  @Override
  protected Transferable createTransferable(JComponent c) {
    List<Item> itemSelection = new ArrayList<Item>();
    int[] selection = jtree.getSelectionRows();
    for (int row : selection) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtree.getPathForRow(row)
          .getLastPathComponent();
      // We get a list of items in the case of period* dragging
      // * : period is used in TracksTreeView : "Less than 6 months" for ie
      if (node.getUserObject() instanceof List) {
        itemSelection.addAll((List<Item>) node.getUserObject());
      } else {
        itemSelection.add((Item) node.getUserObject());
      }
    }
    return new TransferableTreeNodes(itemSelection);
  }

  /**
   * return action type.
   * 
   * @param c DOCUMENT_ME
   * 
   * @return the source actions
   */
  @Override
  public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

}
