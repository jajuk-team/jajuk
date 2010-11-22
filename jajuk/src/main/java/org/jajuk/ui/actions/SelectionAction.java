/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.util.Const;

/**
 * Convenient abstract class to factorize operations on selection.
 */
public abstract class SelectionAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -6072746800882488360L;

  /** DOCUMENT_ME. */
  List<Item> selection = null;

  /** Whether the selection should contain the playlist itself (false) or mapped files (true). */
  boolean expandPlaylists = true;

  /**
   * Instantiates a new selection action.
   *
   * @param msg DOCUMENT_ME
   * @param icon DOCUMENT_ME
   * @param enabled DOCUMENT_ME
   */
  protected SelectionAction(String msg, ImageIcon icon, boolean enabled) {
    super(msg, icon, enabled);
  }

  /**
   * Instantiates a new selection action.
   *
   * @param msg DOCUMENT_ME
   * @param stroke DOCUMENT_ME
   * @param icon DOCUMENT_ME
   * @param enabled DOCUMENT_ME
   */
  public SelectionAction(String name, ImageIcon icon, String stroke, boolean enabled, boolean bHotkey) {
    super(name, icon, stroke, enabled, bHotkey);
  }

  /*
   * This method transforms various entries to a list of items
   *
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void perform(ActionEvent e) throws Exception {
    JComponent source = (JComponent) e.getSource();
    Object o = source.getClientProperty(Const.DETAIL_SELECTION);
    if (o instanceof Item) {
      selection = new ArrayList<Item>(1);
      selection.add((Item) o);
    } else if (o instanceof List) {
      List<Item> list = (List<Item>) o;
      // List of playlists, replace playlist by playlist files
      if (list.size() > 0 && list.get(0) instanceof Playlist) {
        selection = new ArrayList<Item>(10);
        for (Item item : list) {
          Playlist pl = (Playlist) item;
          if (expandPlaylists) {
            selection.addAll(pl.getFiles());
          } else {
            selection.add(pl);
          }
        }
      } else {
        // List of albums, artists ... files or tracks : just perform a cast
        selection = (List<Item>) source.getClientProperty(Const.DETAIL_SELECTION);
      }
    } else if (o instanceof Set) {
      selection = new ArrayList<Item>((Set<Item>) o);
    } else if (o instanceof Playlist) {
      selection = new ArrayList<Item>(1);
      Playlist pl = (Playlist) o;
      if (expandPlaylists) {
        selection.addAll(pl.getFiles());
      } else {
        selection.add(pl);
      }
    }
  }
}
