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
import java.util.ArrayList;
import java.util.List;

import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the next track. Installed keystroke:
 * <code>CTRL + RIGHT ARROW</code>.
 */
public class NextTrackAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new next track action.
   */
  NextTrackAction() {
    super(Messages.getString("JajukWindow.14"), IconLoader.getIcon(JajukIcons.PLAYER_NEXT_SMALL),
        "F10", false, true);
    setShortDescription(Messages.getString("CommandJPanel.9"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    // check modifiers to see if it is a movement inside track, between
    // tracks or between albums
    if (evt != null
    // evt == null when using hotkeys
        && (evt.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
      ActionManager.getAction(JajukActions.NEXT_ALBUM).actionPerformed(evt);
    } else {
      // if playing a radio, launch next radio station
      if (QueueModel.isPlayingRadio()) {
        final List<WebRadio> radios = new ArrayList<WebRadio>(WebRadioManager.getInstance()
            .getWebRadios());
        int index = radios.indexOf(QueueModel.getCurrentRadio());
        if (index == radios.size() - 1) {
          index = 0;
        } else {
          index++;
        }
        final int i = index;
        new Thread("Next Track Thread") {
          @Override
          public void run() {
            QueueModel.launchRadio(radios.get(i));
          }
        }.start();

      } else {
        // Playing a track
        new Thread("Next Track Thread") {
          @Override
          public void run() {
            synchronized (QueueModel.class) {
              try {
                QueueModel.playNext();
              } catch (Exception e) {
                Log.error(e);
              }
            }
          }
        }.start();
      }
    }
  }
}
