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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.Album;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;
import org.jajuk.ui.thumbnails.ThumbnailPopup;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Show a thumb popup on an album.
 * <p>
 * This action is expecting a single album as selection
 * </p>
 * <p>
 * Selection data is provided using the swing properties DETAIL_SELECTION
 * </p>
 */
public class ShowAlbumDetailsAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -8078402652430413821L;

  /**
   * Instantiates a new show album details action.
   */
  ShowAlbumDetailsAction() {
    super(Messages.getString("CatalogView.20"), IconLoader.getIcon(JajukIcons.POPUP), true);
    setShortDescription(Messages.getString("CatalogView.20"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    Object o = source.getClientProperty(Const.DETAIL_SELECTION);
    Album album = null;
    if (o instanceof Album) {
      album = (Album) o;
    } else if (o instanceof List) {
      album = ((List<Album>) o).get(0);
    } else {
      return;
    }
    LocalAlbumThumbnail thumb = new LocalAlbumThumbnail(album, 200, true);
    new ThumbnailPopup(thumb.getDescription(), null, false);
  }
}
