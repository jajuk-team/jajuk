/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JPanel;

import org.jajuk.base.BasicFile;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.util.log.Log;

/**
 *  Dnd support for playlists
 *
 * @author     bflorat
 * @created    13 févr. 2004
 */
/**
 * 
 *  DND handler for playlists
 *
 * @author     bflorat
 * @created    13 févr. 2004
 */
public class PlaylistTransferHandler implements DropTargetListener {
	
	private JPanel jpanel;
	private DropTarget dropTarget; //droptarget
	
	public PlaylistTransferHandler(JPanel jpanel, int action) {
		this.jpanel = jpanel;
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
	}
	
	public final void dropActionChanged(DropTargetDragEvent dtde) {
		int action = dtde.getDropAction();
		dtde.acceptDrag(action);			
	}
	
	public final void drop(DropTargetDropEvent dtde) {
		try {
			int action = dtde.getDropAction();
			Transferable transferable = dtde.getTransferable();
			if (transferable.isDataFlavorSupported(TransferableTreeNode.NODE_FLAVOR)) {
				dtde.acceptDrop(action);				
				dtde.dropComplete(true);
				TransferableTreeNode ttn = (TransferableTreeNode)transferable.getTransferData(TransferableTreeNode.NODE_FLAVOR);
				PlaylistFileItem plfi = (PlaylistFileItem)(((DropTarget)dtde.getSource()).getComponent());
				Object oData = ttn.getData();
				if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NEW){
					if (oData instanceof File){
						plfi.getPlaylistFile().addBasicFile(new BasicFile((File)oData));
						FIFO.getInstance().push((File)oData,true);
					}
				}
				else if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
					if (oData instanceof File){
						Bookmarks.getInstance().addFile((File)oData);
					}
				}
			}
		}		
		catch (Exception e) {	
			Log.error(e);
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}	
	}

}
