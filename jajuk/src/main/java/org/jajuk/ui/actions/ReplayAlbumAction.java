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
 *  $$Revision:3308 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.Player;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the begining of current album
 */
public class ReplayAlbumAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  ReplayAlbumAction() {
    super("replay album", "alt F9", false, true);
  }

  @Override
  public void perform(ActionEvent evt) {
    new Thread() {
      @Override
      public void run() {
        synchronized (QueueModel.class) {
          try {
            File current = QueueModel.getPlayingFile();
            if (current != null) {
              List<Track> tracks = current.getTrack().getAlbum().getTracksCache();
              List<File> files = new ArrayList<File>(tracks.size());
              for (Track track : tracks) {
                files.add(track.getPlayeableFile(true));
              }
              QueueModel.push(UtilFeatures.createStackItems(files, Conf
                  .getBoolean(Const.CONF_STATE_REPEAT), true), false);
            }
          } catch (Exception e) {
            Log.error(e);
          }
          if (Player.isPaused()) { // player was paused, reset
            // pause button
            // when changing of track
            Player.setPaused(false);
            ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_RESUME));
          }
        }
      }
    }.start();
  }
}
