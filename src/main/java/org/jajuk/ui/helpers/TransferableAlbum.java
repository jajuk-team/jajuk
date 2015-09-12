/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.ui.helpers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import org.jajuk.base.Album;
import org.jajuk.base.TrackManager;

/**
 * Transferable album ( for DND ).
 */
public class TransferableAlbum implements Transferable {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** The Constant ALBUM_FLAVOR.   */
  public static final DataFlavor ALBUM_FLAVOR = new DataFlavor(
      DataFlavor.javaJVMLocalObjectMimeType, "Album");
  private Album album;

  /**
   * Instantiates a new transferable album.
   * 
   * @param album 
   */
  public TransferableAlbum(Album album) {
    this.album = album;
  }

  private final DataFlavor[] flavors = { ALBUM_FLAVOR };

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
    if (flavor == ALBUM_FLAVOR) {
      return this;
    }
    throw new UnsupportedFlavorException(flavor);
  }

  /**
   * Gets the user object.
   * 
   * @return associated album
   */
  public Object getUserObject() {
    return TrackManager.getInstance().getAssociatedTracks(this.album, true);
  }
}
