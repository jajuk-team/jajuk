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
 * $Revision: 3216 $
 */

package org.jajuk.ui.widgets;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.base.Playlist;
import org.jajuk.base.Playlist.Type;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

/**
 * A physical playlist icon + text
 */
public class SmartPlaylist extends JPanel {
  private static final long serialVersionUID = 1L;

  /** Icon */
  private JLabel jlIcon;

  private Type type;
  
  /**Associated playlist*/
  private Playlist plf;

  /**
   * Constructor
   * 
   * @param iType :
   *          playlist type (see Type enum)
   */
  public SmartPlaylist(Playlist.Type type) {
    this.type = type;
    if (type == Type.NEW) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_NEW);
      plf = new Playlist(Type.NEW, null, null, null);
    } else if (type == Type.BESTOF) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_BESTOF);
      plf = new Playlist(Type.BESTOF, null, null, null);
    } else if (type == Type.BOOKMARK) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_BOOKMARK);
      plf = new Playlist(Type.BOOKMARK, null, null, null);
    } else if (type == Type.NOVELTIES) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_NOVELTIES);
      plf = new Playlist(Type.NOVELTIES, null, null, null);
    }
    jlIcon.setPreferredSize(new Dimension(50, 50));
    setToolTipText(getName());
    add(jlIcon);
    // new PlaylistTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);
  }

  
  /**
   * @return Returns the Type.
   */
  public Type getType() {
    return type;
  }
  
  /**
   * @return Associated playlist
   */
  public Playlist getPlaylist() {
    return plf;
  }

  /**
   * Get a name for this playlist item
   * 
   * @return playlist item name ( playlist name or label for special ones )
   */
  public String getName() {
    String sOut = "";
    if (type == Type.NEW) {
      sOut = Messages.getString("PlaylistFileItem.2");
    } else if (type == Type.BESTOF) {
      sOut = Messages.getString("PlaylistFileItem.4");
    } else if (type == Type.BOOKMARK) {
      sOut = Messages.getString("PlaylistFileItem.3");
    } else if (type == Type.NOVELTIES) {
      sOut = Messages.getString("PlaylistFileItem.1");
    }
    return sOut;
  }

  /**
   * @return icon
   */
  public JLabel getIcon() {
    return this.jlIcon;
  }
}
