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

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jajuk.base.Album;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;

/**
 * DND handler for table.
 */

public class CatalogViewTransferHandler extends TransferHandler {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private final LocalAlbumThumbnail item;

  /**
   * Constructor.
   * 
   * @param item DOCUMENT_ME
   */
  public CatalogViewTransferHandler(LocalAlbumThumbnail item) {
    this.item = item;
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
    Object o = item.getItem();
    if (o != null) {
      return new TransferableAlbum((Album) o);
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

  /**
   * Called when dropping, no drop in catalog view for now.
   * 
   * @param c DOCUMENT_ME
   * @param t DOCUMENT_ME
   * 
   * @return true, if import data
   */
  @Override
  public boolean importData(JComponent c, Transferable t) {
    return false;
  }

  /* (non-Javadoc)
   * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
   */
  @Override
  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    return false;
  }

}
