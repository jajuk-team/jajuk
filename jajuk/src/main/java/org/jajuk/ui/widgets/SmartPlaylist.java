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

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.base.PlaylistFile;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

/**
 * A physical playlist icon + text
 */
public class SmartPlaylist extends JPanel {
  private static final long serialVersionUID = 1L;

  /** Associated playlist file */
  private PlaylistFile plf;

  /** Icon */
  private JLabel jlIcon;

  /** Playlist file type */
  public enum Type {
    NEW, BOOKMARK, BESTOF, NOVELTIES
  }

  private Type type;

  /**
   * Constructor
   * 
   * @param iType :
   *          Playlist file type (see Type enum)
   */
  public SmartPlaylist(Type type) {
    this.type = type;
    double[][] dSize = { { 200 }, { 40, 5 } };
    TableLayout layout = new TableLayout(dSize);
    layout.setVGap(5);
    setLayout(layout);
    if (type == Type.NEW) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_NEW);
    } else if (type == Type.BESTOF) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_BESTOF);
    } else if (type == Type.BOOKMARK) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_BOOKMARK);
    } else if (type == Type.NOVELTIES) {
      jlIcon = new JLabel(IconLoader.ICON_PLAYLIST_NOVELTIES);
    }
    jlIcon.setPreferredSize(new Dimension(100, 100));
    JLabel jlName = new JLabel(getName());
    jlName.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN_S));
    add(jlIcon, "0,0,c,c");
    add(jlName, "0,1,c,c");
    // new PlaylistTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);
  }

  /**
   * @return Returns the playlist file.
   */
  public PlaylistFile getPlaylistFile() {
    return plf;
  }

  /**
   * @return Returns the Type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Get a name for this playlist file item
   * 
   * @return playlist file item name ( playlist name or label for special ones )
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
