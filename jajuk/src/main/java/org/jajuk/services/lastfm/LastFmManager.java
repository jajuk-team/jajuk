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
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * LastFM Manager, handle file launch events to submit informations among others
 * things
 * <p>
 * singleton
 * </p>
 */
public class LastFmManager implements Observer, ITechnicalStrings {
  /** Self instance */
  private static LastFmManager self;

  private LastFmManager() {
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // Display an hideable message to user if audioscrobber is disable
    // Show this message only one time by jajuk session
    if (!ConfigurationManager.getBoolean(CONF_AUDIOSCROBBLER_ENABLE)
    // don't dhow this message if first jajuk launch: already too many
        // popups
        && !org.jajuk.Main.bFirstSession) {
      Messages.showHideableWarningMessage(Messages.getString("LastFmManager.0"),
          CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED);
    }
  }

  static public LastFmManager getInstance() {
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
  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_FINISHED);
    return eventSubjectSet;
  }

  public void configure() {
    Submitter.setPassword(Util
        .rot13(ConfigurationManager.getProperty(CONF_AUDIOSCROBBLER_PASSWORD)));
    Submitter.setUser(ConfigurationManager.getProperty(CONF_AUDIOSCROBBLER_USER));
    Submitter.setProxy(DownloadManager.getProxy());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(final Event event) {
    if (EventSubject.EVENT_FILE_FINISHED == event.getSubject()) {
      new Thread() {
        public void run() {
          if (ConfigurationManager.getBoolean(CONF_AUDIOSCROBBLER_ENABLE)) {
            File file = (File) event.getDetails().get(DETAIL_CURRENT_FILE);
            long playedTime = file.getTrack().getDuration();
            // If we are in intro mode, computes actually listened
            // time
            if (ConfigurationManager.getBoolean(CONF_STATE_INTRO)) {
              playedTime = (playedTime * ConfigurationManager.getInt(CONF_OPTIONS_INTRO_BEGIN) / 100)
                  - ConfigurationManager.getInt(CONF_OPTIONS_INTRO_BEGIN);
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
