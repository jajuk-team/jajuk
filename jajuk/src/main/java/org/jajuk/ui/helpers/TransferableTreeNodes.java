/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jajuk.base.Item;

/**
 * Transferable tree nodes ( for DND ).
 */
public class TransferableTreeNodes implements Transferable {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant NODE_FLAVOR.  DOCUMENT_ME */
  public static final DataFlavor NODE_FLAVOR = new DataFlavor(
      DataFlavor.javaJVMLocalObjectMimeType, "Node");

  /** Transferable model. */
  private List<Item> items;

  /**
   * Instantiates a new transferable tree node.
   * 
   * @param items DOCUMENT_ME
   */
  public TransferableTreeNodes(List<Item> items) {
    this.items = items;
  }

  /** DOCUMENT_ME. */
  private final DataFlavor[] flavors = { NODE_FLAVOR };

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
   */
  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return Arrays.asList(flavors).contains(flavor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
   */
  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor == NODE_FLAVOR) {
      return this;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  /**
   * Gets the user object.
   * 
   * @return the tranferable data
   */
  public Object getUserObject() {
    return items;
  }
}
