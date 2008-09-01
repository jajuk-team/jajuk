/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.services.lastfm;

import ext.services.lastfm.Submitter;
import ext.services.lastfm.SubmitterException;

import java.util.HashSet;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * LastFM Manager, handle file launch events to submit informations among others
 * things
 * <p>
 * singleton
 * </p>
 */
public final class LastFmManager implements Observer, Const {
  /** Self instance */
  private static LastFmManager self;

  private LastFmManager() {
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // Display an hideable message to user if audioscrobber is disable
    // Show this message only one time by jajuk session
    if (!Conf.getBoolean(CONF_LASTFM_ENABLE)
    // don't show this message if first jajuk launch: already too many
        // popups.
        && !UpgradeManager.isFirstSesion()
        // don't show neither if last.fm login is already provided but disabled
        && UtilString.isVoid(Conf.getString(CONF_LASTFM_USER))) {
      Messages.showHideableWarningMessage(Messages.getString("LastFmManager.0"),
          CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED);
    }
  }

  public static synchronized LastFmManager getInstance() {
    if (self == null) {
      self = new LastFmManager();
      // populate configuration
      self.configure();
    }
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_FINISHED);
    return eventSubjectSet;
  }

  public void configure() {
    Submitter.setPassword(UtilString.rot13(Conf.getString(CONF_LASTFM_PASSWORD)));
    Submitter.setUser(Conf.getString(CONF_LASTFM_USER));
    Submitter.setProxy(DownloadManager.getProxy());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(final Event event) {
    if (JajukEvents.FILE_FINISHED == event.getSubject()) {
      new Thread() {
        @Override
        public void run() {
          if (Conf.getBoolean(CONF_LASTFM_ENABLE)) {
            File file = (File) event.getDetails().get(DETAIL_CURRENT_FILE);
            long playedTime = file.getTrack().getDuration();
            // If we are in intro mode, computes actually listened
            // time
            if (Conf.getBoolean(CONF_STATE_INTRO)) {
              playedTime = (playedTime * Conf.getInt(CONF_OPTIONS_INTRO_BEGIN) / 100)
                  - Conf.getInt(CONF_OPTIONS_INTRO_BEGIN);
            }
            try {
              Submitter.submitTrack(file.getTrack(), playedTime);
            } catch (SubmitterException e) {
              Log.error(e);
            }
          }

        }
      }.start();
    }
  }
}
