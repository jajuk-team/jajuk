/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.base.Observer;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.ui.helpers.PlaylistFileItem;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

/**
 * Shows playlist files
 * <p>
 * Physical perspective *
 */
public class PhysicalPlaylistRepositoryView extends AbstractPlaylistRepositoryView implements
    Observer, ActionListener {

  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("PhysicalPlaylistRepositoryView.6");
  }

  /**
   * Create playlists from collection
   */
  public synchronized void populatePlaylists() {
    super.populatePlaylists();
    // normal playlists
    Iterator it = PlaylistFileManager.getInstance().getPlaylistFiles().iterator();
    while (it.hasNext()) {
      PlaylistFile plf = (PlaylistFile) it.next();
      if (plf.shouldBeHidden()) {
        continue;
      }
      PlaylistFileItem plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NORMAL,
          IconLoader.ICON_PLAYLIST_NORMAL, plf, plf.getName());
      alPlaylistFileItems.add(plfi);
      plfi.addMouseListener(ma);
      plfi.setToolTipText(plf.getAbsolutePath());
      jpRoot.add(plfi);
      if (plfiSelected != null && plfi.getPlaylistFile().equals(plfiSelected.getPlaylistFile())) {
        plfiSelected = plfi;
      }
    }
  }

  public synchronized void removeItem(PlaylistFileItem plfiSelected) {
    if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)) { // file
      // delete confirmation
      String sFileToDelete = plfiSelected.getPlaylistFile().getAbsolutePath();
      String sMessage = Messages.getString("Confirmation_delete") + "\n" + sFileToDelete;
      int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (i == JOptionPane.YES_OPTION) {
        PlaylistFileManager.getInstance().removePlaylistFile(plfiSelected.getPlaylistFile());
      }
    }
  }

  public void play(PlaylistFileItem plfi) throws JajukException {
    plfi.getPlaylistFile().play();
  }
}
