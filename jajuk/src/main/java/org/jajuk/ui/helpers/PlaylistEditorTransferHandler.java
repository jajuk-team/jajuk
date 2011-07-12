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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.views.PlaylistView;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * DND handler for table.
 */

public class PlaylistEditorTransferHandler extends TableTransferHandler {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * 
   * @param jtable DOCUMENT_ME
   */
  public PlaylistEditorTransferHandler(final JajukTable jtable) {
    super(jtable);
  }

  /**
   * Called when dropping.
   * 
   * @param c DOCUMENT_ME
   * @param t DOCUMENT_ME
   * 
   * @return true, if import data
   */
  @SuppressWarnings("unchecked")
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
        JajukTable jtable = view.getTable();
        // fetch the drop location
        int row = jtable.getDropRow();
        Playlist plf = view.getCurrentPlaylist();
        Object oData = null;
        DataFlavor flavor = t.getTransferDataFlavors()[0];
        if (flavor.getHumanPresentableName().equals(
            TransferableTableRows.ROW_FLAVOR.getHumanPresentableName())) {
          TransferableTableRows ttr = (TransferableTableRows) t
              .getTransferData(TransferableTableRows.ROW_FLAVOR);
          oData = ttr.getUserObject();
        } else if (flavor.getHumanPresentableName().equals(
            TransferableTreeNodes.NODE_FLAVOR.getHumanPresentableName())) {
          TransferableTreeNodes ttn = (TransferableTreeNodes) t
              .getTransferData(TransferableTreeNodes.NODE_FLAVOR);
          oData = ttn.getUserObject();
        } else if (flavor.getHumanPresentableName().equals(
            TransferableAlbum.ALBUM_FLAVOR.getHumanPresentableName())) {
          TransferableAlbum ttn = (TransferableAlbum) t
              .getTransferData(TransferableAlbum.ALBUM_FLAVOR);
          oData = ttn.getUserObject();
        }

        List<File> alSelectedFiles = null;
        try {
          alSelectedFiles = UtilFeatures.getFilesForItems((List<Item>) oData);
        } catch (JajukException je) {
          Log.error(je);
          Messages.showErrorMessage(je.getCode());
          return false;
        }

        // If we get zero playing files, just leave, do not display a dummy message in Queue code:
        if (alSelectedFiles.size() == 0) {
          return false;
        }
        // row = -1 if none item in the table or if we drop after the last row,
        // we set table's size as an index
        if (row < 0) {
          row = plf.getFiles().size();
        }

        // queue case
        if (plf.getType() == Playlist.Type.QUEUE) {
          // If user selected "push on drop" option just push the selection
          if (Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_DROP)) {
            QueueModel.push(
                UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(alSelectedFiles),
                    Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), true), true);
          } else {
            // Insert the selection at drop target
            QueueModel.insert(
                UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(alSelectedFiles),
                    Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), true), row);
          }
        }
        // normal or new playlist case
        else if (plf.getType() == Playlist.Type.NORMAL || plf.getType() == Playlist.Type.NEW
            || plf.getType() == Playlist.Type.BOOKMARK) {
          //  By default, inset at the end of the playlist
          int position = plf.getNbOfTracks() - 1;
          if (position < 0) {
            position = 0;
          }
          if (!Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_DROP)) {
            position = row;
          }
          view.importFiles(UtilFeatures.applyPlayOption(alSelectedFiles), position);
        }
        return true;
      }
    } catch (Exception e) {
      Log.error(e);
    } finally {
      jtable.getSelectionModel().setValueIsAdjusting(false);
    }
    return false;

  }

  /* (non-Javadoc)
   * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
   */
  @Override
  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    String sFlavor = flavors[0].getHumanPresentableName();
    if ("Node".equals(sFlavor) || "Row".equals(sFlavor) || "Album".equals(sFlavor)) {
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
