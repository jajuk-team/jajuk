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
package org.jajuk.ui.actions;

import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.jajuk.services.players.Player;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Action of clicking on the mute button.
 */
public class MuteAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new mute action.
   */
  MuteAction() {
    super(Messages.getString("JajukWindow.2"), IconLoader.getIcon(JajukIcons.VOLUME_LEVEL1), "F8",
        true, true);
    setShortDescription(Messages.getString("JajukWindow.19"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    Player.mute();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (Player.isMuted()) {
          setName(Messages.getString("JajukWindow.1"));
        } else {
          setName(Messages.getString("JajukWindow.2"));
        }
        setVolumeIcon(100 * Player.getCurrentVolume());
      }
    });
  }

  /**
   * Set Volume Icon.
   * 
   * @param fVolume DOCUMENT_ME
   */
  public static void setVolumeIcon(final float fVolume) {
    if (fVolume <= 0 || Player.isMuted()) {
      // We need to check if player is mute to handle cases when volume > 0
      // and user muted jajuk in stopped state.
      ActionManager.getAction(MUTE_STATE).setIcon(IconLoader.getIcon(JajukIcons.MUTED));
    } else if (fVolume <= 33) {
      ActionManager.getAction(MUTE_STATE).setIcon(IconLoader.getIcon(JajukIcons.VOLUME_LEVEL1));
    } else if (fVolume <= 66) {
      ActionManager.getAction(MUTE_STATE).setIcon(IconLoader.getIcon(JajukIcons.VOLUME_LEVEL2));
    } else {
      ActionManager.getAction(MUTE_STATE).setIcon(IconLoader.getIcon(JajukIcons.VOLUME_LEVEL3));
    }
  }
}
