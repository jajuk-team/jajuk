/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

package org.jajuk.ui;

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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JTable;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.views.AbstractPlaylistEditorView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * DND handler for playlists
 * 
 * @author Bertrand Florat
 * @created 13 feb. 2004
 */
public class PlaylistTransferHandler implements DropTargetListener,
		ITechnicalStrings {

	private Component jpanel;

	private DropTarget dropTarget; // droptarget

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
		} else if (c instanceof AbstractPlaylistEditorView) {
			plfi = ((AbstractPlaylistEditorView) c)
					.getCurrentPlaylistFileItem();
		} else if (c instanceof JTable) {
			c = c.getParent().getParent().getParent();
			plfi = ((AbstractPlaylistEditorView) c)
					.getCurrentPlaylistFileItem();
		}
		if (plfi != null
				&& (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF || plfi
						.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES)) { // no
			// dnd
			// to
			// best
			// of
			// playlist
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
			} else if (c instanceof AbstractPlaylistEditorView) {
				plfi = ((AbstractPlaylistEditorView) c)
						.getCurrentPlaylistFileItem();
			} else if (c instanceof JTable) {
				c = c.getParent().getParent().getParent();
				plfi = ((AbstractPlaylistEditorView) c)
						.getCurrentPlaylistFileItem();
			}
			if (plfi == null) {
				return;
			}
			Object oData = null;
			if (transferable
					.isDataFlavorSupported(TransferableTreeNode.NODE_FLAVOR)
					|| transferable
							.isDataFlavorSupported(TransferableTableRow.ROW_FLAVOR)) {
				String sFlavor = (Arrays.asList(transferable
						.getTransferDataFlavors()).get(0))
						.getHumanPresentableName();
				if (sFlavor.equals("Node")) { //$NON-NLS-1$
					dtde.acceptDrop(action);
					dtde.dropComplete(true);
					TransferableTreeNode ttn = (TransferableTreeNode) transferable
							.getTransferData(TransferableTreeNode.NODE_FLAVOR);
					oData = ttn.getData();
				} else if (sFlavor.equals("Row")) { //$NON-NLS-1$
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
			ArrayList<File> alSelectedFiles = new ArrayList<File>(100);
			// computes logical selection if any
			Set<Track> alLogicalTracks = null;
			if (oData instanceof Style || oData instanceof Author
					|| oData instanceof Album || oData instanceof Track) {
				if (oData instanceof Style || oData instanceof Author
						|| oData instanceof Album) {
					alLogicalTracks = TrackManager.getInstance()
							.getAssociatedTracks((Item) oData);
				} else if (oData instanceof Track) {
					alLogicalTracks = new LinkedHashSet<Track>(100);
					alLogicalTracks.add((Track) oData);
				}
				// prepare files
				if (alLogicalTracks != null && alLogicalTracks.size() > 0) {
					Iterator it = alLogicalTracks.iterator();
					while (it.hasNext()) {
						Track track = (Track) it.next();
						File file = track.getPlayeableFile(false);
						if (file == null) { // none mounted file for this
							// track
							continue;
						}
						alSelectedFiles.add(file);
					}
				}
			}
			// computes physical selection if any
			else if (oData instanceof File || oData instanceof Directory
					|| oData instanceof Device) {
				if (oData instanceof File) {
					alSelectedFiles.add((File) oData);
				} else if (oData instanceof Directory) {
					alSelectedFiles = ((Directory) oData).getFilesRecursively();
				} else if (oData instanceof Device) {
					alSelectedFiles = ((Device) oData).getFilesRecursively();
				}
			}
			// display a warning message if none accessible file can found
			// for these tracks
			if (alSelectedFiles.size() == 0) {
				Messages.showWarningMessage(Messages.getErrorMessage("018"));//$NON-NLS-1$
				return;
			}
			// queue case
			if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
				FIFO.getInstance().push(
						Util.createStackItems(Util
								.applyPlayOption(alSelectedFiles),
								ConfigurationManager
										.getBoolean(CONF_STATE_REPEAT), true),
						ConfigurationManager
								.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_DROP));
			}
			// bookmark case
			else if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
				Bookmarks.getInstance().addFiles(
						Util.applyPlayOption(alSelectedFiles));
			}
			// normal or new playlist case
			else if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NORMAL
					|| plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NEW) {
				plfi.getPlaylistFile().addFiles(
						Util.applyPlayOption(alSelectedFiles));
			}
		} catch (Exception e) {
			Log.error(e);
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}
	}
}
