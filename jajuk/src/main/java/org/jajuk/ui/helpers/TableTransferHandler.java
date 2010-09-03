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

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.jajuk.base.FileManager;
import org.jajuk.ui.widgets.JajukTable;

/**
 * DND handler for table.
 */

public class TableTransferHandler extends TransferHandler {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  final JTable jtable;

  /** DOCUMENT_ME. */
  static int iSelectedRow = 0;

  /**
   * Constructor.
   * 
   * @param jtable DOCUMENT_ME
   */
  public TableTransferHandler(final JTable jtable) {
    this.jtable = jtable;
    DragSource source = DragSource.getDefaultDragSource();
    // Override the drag gesture recognizer as it doesn't work well when draging from a jtable :
    // 1 select row 1
    // 2 start draging row 2 : the drag gesture is not recognized because the row 2 is not yet selected
    source.createDefaultDragGestureRecognizer(jtable, DnDConstants.ACTION_COPY,
        new DragGestureListener() {

          @Override
          public void dragGestureRecognized(DragGestureEvent dge) {
            Transferable transferable = createTransferable(jtable);

            //and this is the magic right here
            dge.startDrag(null, transferable);
          }
        });
  }

  /**
   * Called when dragging.
   * 
   * @param c DOCUMENT_ME
   * 
   * @return the transferable
   */
  @Override
  protected Transferable createTransferable(JComponent c) {
    iSelectedRow = jtable.getSelectionModel().getMinSelectionIndex();
    // make sure to remove others selected rows (can occur during the drag)
    jtable.getSelectionModel().setSelectionInterval(iSelectedRow, iSelectedRow);
    if (jtable instanceof JajukTable) {// sorting only for jajuk table
      // selected row in model
      iSelectedRow = ((JajukTable) jtable).convertRowIndexToModel(iSelectedRow);
    }
    Object o = ((JajukTableModel) jtable.getModel()).getItemAt(iSelectedRow);
    if (o == null) { // no? try to find a file for this id
      o = FileManager.getInstance().getFileByID(
          jtable.getModel().getValueAt(iSelectedRow, 0).toString());
    }
    if (o != null) {
      return new TransferableTableRow(o);
    }
    return null;
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
