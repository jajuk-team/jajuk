/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 *  $Revision: 2118 $
 */

package org.jajuk.ui.helpers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.views.PlaylistView;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * DND handler for table
 */

public class PlaylistEditorTransferHandler extends TransferHandler {

  private static final long serialVersionUID = 1L;

  private JTable jtable;

  private static int iSelectedRow = 0;

  /** Constructor */
  public PlaylistEditorTransferHandler(JTable jtable) {
    this.jtable = jtable;
  }

  /**
   * Called when dragging
   */
  @Override
  protected Transferable createTransferable(JComponent c) {
    // make sure to remove others selected rows (can occur during the drag)
    jtable.getSelectionModel().setSelectionInterval(iSelectedRow, iSelectedRow);
    if (jtable instanceof JajukTable) {// sorting only for jajuk table
      iSelectedRow = ((JajukTable) jtable).convertRowIndexToModel(iSelectedRow);
      // selected row in model
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
   * return action type
   */
  @Override
  public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  /**
   * Called when dropping
   */
  @Override
  public boolean importData(JComponent c, Transferable t) {
    try {
      if (canImport(c, t.getTransferDataFlavors())) {
        // Note that component hierarchy is different between queue and playlist
        // view
        JComponent comp = (JComponent) c.getParent();
        while (!(comp instanceof PlaylistView)) {
          comp = (JComponent) comp.getParent();
        }
        PlaylistView view = ((PlaylistView) comp);
        Playlist plf = view.getCurrentPlaylist();
        Object oData = null;
        DataFlavor flavor = t.getTransferDataFlavors()[0];
        if (flavor.getHumanPresentableName().equals(
            TransferableTableRow.ROW_FLAVOR.getHumanPresentableName())) {
          TransferableTableRow ttr = (TransferableTableRow) t
              .getTransferData(TransferableTableRow.ROW_FLAVOR);
          oData = ttr.getData();
        } else if (flavor.getHumanPresentableName().equals(
            TransferableTreeNode.NODE_FLAVOR.getHumanPresentableName())) {
          TransferableTreeNode ttn = (TransferableTreeNode) t
              .getTransferData(TransferableTreeNode.NODE_FLAVOR);
          oData = ttn.getData();
        } else if (flavor.getHumanPresentableName().equals(
            TransferableAlbum.ALBUM_FLAVOR.getHumanPresentableName())) {
          TransferableAlbum ttn = (TransferableAlbum) t
              .getTransferData(TransferableAlbum.ALBUM_FLAVOR);
          oData = ttn.getData();
        }
        List<File> alSelectedFiles = UtilFeatures.getPlayableFiles((Item) oData);
        // queue case
        if (plf.getType() == Playlist.Type.QUEUE) {
          FIFO.push(UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(alSelectedFiles),
              Conf.getBoolean(Const.CONF_STATE_REPEAT), true), Conf
              .getBoolean(Const.CONF_OPTIONS_DEFAULT_ACTION_DROP));
        }
        // normal or new playlist case
        else if (plf.getType() == Playlist.Type.NORMAL || plf.getType() == Playlist.Type.NEW
            || plf.getType() == Playlist.Type.BOOKMARK) {
          view.importFiles(UtilFeatures.applyPlayOption(alSelectedFiles));
        }
        return true;
      }
    } catch (Exception e) {
      Log.error(e);
    }
    return false;

  }

  @Override
  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    String sFlavor = flavors[0].getHumanPresentableName();
    if (sFlavor.equals("Node") || sFlavor.equals("Row") || sFlavor.equals("Album")) {
      JComponent comp = (JComponent) c.getParent();
      while (!(comp instanceof PlaylistView)) {
        comp = (JComponent) comp.getParent();
      }
      PlaylistView view = ((PlaylistView) comp);
      Playlist plf = view.getCurrentPlaylist();
      // Don't accept drop for novelties and bestof
      return (plf.getType() != Playlist.Type.NOVELTIES && plf.getType() != Playlist.Type.BESTOF);
    }
    return false;
  }

}
