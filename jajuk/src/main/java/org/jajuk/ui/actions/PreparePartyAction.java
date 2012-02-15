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
 *  
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.ui.wizard.PreparePartyWizard;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

/**
 * DOCUMENT_ME.
 */
public class PreparePartyAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new prepare party action.
   */
  PreparePartyAction() {
    super(Messages.getString("AbstractPlaylistEditorView.27"), IconLoader
        .getIcon(JajukIcons.PREPARE_PARTY), true);
    setShortDescription(Messages.getString("AbstractPlaylistEditorView.27"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void perform(ActionEvent e) throws JajukException {
    JComponent source = (JComponent) e.getSource();

    // if we get a playlist, pass it on so that the Wizard does not provide a
    // choice of where to take the tracks anymore
    Object o = source.getClientProperty(Const.DETAIL_SELECTION);
    if (o != null) {
      // If action call comes from a tree, the selection is returned as a list
      final Playlist playlist;
      if (o instanceof List) {
        playlist = (Playlist) ((List<Item>) o).get(0);
      } else {
        playlist = ((Playlist) o);
      }

      // indicate to the Wizard that it should use the pre-built Playlist and
      // not provide the first selection screen. We need to do this in a static
      // method before creation because the Wizard needs to use this during
      // construction already
      PreparePartyWizard.setPlaylist(playlist);
      PreparePartyWizard wizard = new PreparePartyWizard(true);
      wizard.show();
    } else {
      // without playlist, just display the general wizard that allows all kinds
      // of sources for the tracks
      PreparePartyWizard wizard = new PreparePartyWizard(false);
      wizard.show();
    }
  }
}
