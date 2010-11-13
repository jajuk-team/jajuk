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

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the begining of current album.
 */
public class ReplayAlbumAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new replay album action.
   */
  ReplayAlbumAction() {
    super("replay album", "alt F9", false, true);
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    new Thread("Replay Album Thread") {
      @Override
      public void run() {
        synchronized (QueueModel.class) {
          try {
            File current = QueueModel.getPlayingFile();
            if (current != null) {
              // Create a new cache list to avoid synchronization of the album cache
              List<Track> tracks = new ArrayList<Track>(current.getTrack().getAlbum()
                  .getTracksCache());
              List<File> files = null;
              files = new ArrayList<File>(tracks.size());
              for (Track track : tracks) {
                files.add(track.getBestFile(true));
              }
              QueueModel.resetAround(QueueModel.getIndex(), current.getTrack().getAlbum());
              QueueModel.push(UtilFeatures.createStackItems(files, Conf
                  .getBoolean(Const.CONF_STATE_REPEAT_ALL), true), false);
            }
          } catch (Exception e) {
            Log.error(e);
          }
        }
      }
    }.start();
  }
}
