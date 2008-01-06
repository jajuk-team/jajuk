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

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JTable;

import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.services.bookmark.Bookmarks;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.views.PlaylistEditorView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * DND handler for playlists
 */
public class PlaylistTransferHandler implements DropTargetListener, ITechnicalStrings {

  private Component jpanel;

  /** Specific drop target, do not remove this variable */
  private DropTarget dropTarget;

  public PlaylistTransferHandler(Component c, int action) {
    this.jpanel = c;
    dropTarget = new DropTarget(jpanel, action, this);
  }

  public final void dragExit(DragSourceEvent dse) {
    dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  /* Methods for DropTargetListener */

  public final void dragEnter(DropTargetDragEvent dtde) {
  }

  public final void dragExit(DropTargetEvent dte) {
  }

  public final void dragOver(DropTargetDragEvent dtde) {
    Component c = ((DropTarget) dtde.getSource()).getComponent();
    PlaylistFileItem plfi = null;
    if (c instanceof PlaylistFileItem) {
      plfi = (PlaylistFileItem) c;
    } else if (c instanceof PlaylistEditorView) {
      plfi = ((PlaylistEditorView) c).getCurrentPlaylistFileItem();
    } else if (c instanceof JTable) {
      c = c.getParent().getParent().getParent();
      plfi = ((PlaylistEditorView) c).getCurrentPlaylistFileItem();
    }
    // no dnd to best of playlist
    if (plfi != null
        && (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES)) {
      dtde.rejectDrag();
    }
  }

  public final void dropActionChanged(DropTargetDragEvent dtde) {
    int action = dtde.getDropAction();
    dtde.acceptDrag(action);
  }

  public final void drop(DropTargetDropEvent dtde) {
    try {
      int action = dtde.getDropAction();
      Transferable transferable = dtde.getTransferable();
      Component c = ((DropTarget) dtde.getSource()).getComponent();
      PlaylistFileItem plfi = null;
      if (c instanceof PlaylistFileItem) {
        plfi = (PlaylistFileItem) c;
      } else if (c instanceof PlaylistEditorView) {
        plfi = ((PlaylistEditorView) c).getCurrentPlaylistFileItem();
      } else if (c instanceof JTable) {
        c = c.getParent().getParent().getParent();
        plfi = ((PlaylistEditorView) c).getCurrentPlaylistFileItem();
      }
      if (plfi == null) {
        return;
      }
      Object oData = null;
      if (transferable.isDataFlavorSupported(TransferableTreeNode.NODE_FLAVOR)
          || transferable.isDataFlavorSupported(TransferableTableRow.ROW_FLAVOR)) {
        String sFlavor = (Arrays.asList(transferable.getTransferDataFlavors()).get(0))
            .getHumanPresentableName();
        if (sFlavor.equals("Node")) {
          dtde.acceptDrop(action);
          dtde.dropComplete(true);
          TransferableTreeNode ttn = (TransferableTreeNode) transferable
              .getTransferData(TransferableTreeNode.NODE_FLAVOR);
          oData = ttn.getData();
        } else if (sFlavor.equals("Row")) {
          dtde.acceptDrop(action);
          dtde.dropComplete(true);
          TransferableTableRow ttr = (TransferableTableRow) transferable
              .getTransferData(TransferableTableRow.ROW_FLAVOR);
          oData = ttr.getData();
        } else {
          dtde.rejectDrop();
          dtde.dropComplete(false);
        }
      }
      // computes selection
      ArrayList<File> alSelectedFiles = Util.getPlayableFiles((Item) oData);
      // display a warning message if none accessible file can found
      // for these tracks
      if (alSelectedFiles.size() == 0) {
        Messages.showWarningMessage(Messages.getErrorMessage(18));
        return;
      }
      // queue case
      if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
        FIFO.getInstance().push(
            Util.createStackItems(Util.applyPlayOption(alSelectedFiles), ConfigurationManager
                .getBoolean(CONF_STATE_REPEAT), true),
            ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_DROP));
      }
      // bookmark case
      else if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
        Bookmarks.getInstance().addFiles(Util.applyPlayOption(alSelectedFiles));
      }
      // normal or new playlist case
      else if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NORMAL
          || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NEW) {
        plfi.getPlaylistFile().addFiles(Util.applyPlayOption(alSelectedFiles));
      }
    } catch (Exception e) {
      Log.error(e);
      dtde.rejectDrop();
      dtde.dropComplete(false);
    }
  }
}
