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
package org.jajuk.services.lastfm;

import ext.services.lastfm.LastFmService;
import ext.services.lastfm.ScrobblerException;

import java.util.HashSet;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * LastFM Manager, handle file launch events to submit informations among others
 * things.
 * <p>
 * singleton
 * </p>
 */
public final class LastFmManager implements Observer, Const {
  /** Self instance. */
  private static LastFmManager self = new LastFmManager();
  /** Lastfm service. */
  private LastFmService service;

  /**
   * Instantiates a new last fm manager.
   */
  private LastFmManager() {
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // Create the service
    service = LastFmService.getInstance();
  }

  /**
   * Gets the single instance of LastFmManager.
   * 
   * @return single instance of LastFmManager
   */
  public static LastFmManager getInstance() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_FINISHED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  @Override
  public void update(final JajukEvent event) {
    if (Conf.getBoolean(Const.CONF_LASTFM_AUDIOSCROBBLER_ENABLE)
        && JajukEvents.FILE_FINISHED == event.getSubject()
        && !Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      final File file = (File) event.getDetails().get(Const.DETAIL_CURRENT_FILE);
      if (!file.getTrack().getBooleanValue(XML_TRACK_SCROBBLE)) {
        Log.debug("Track scrobble property unset, not submitted to last.fm : "
            + file.getTrack().getID());
        return;
      }
      new Thread("LastFM Update Thread") {
        @Override
        public void run() {
          long playedTime = (Long) event.getDetails().get(Const.DETAIL_CONTENT);
          // Last.FM rule : only submit >= 30secs playbacks
          if (playedTime >= 30000) {
            try {
              service.submit(file.getTrack(), playedTime);
            } catch (ScrobblerException e) {
              Log.error(e);
            }
          } else {
            Log.info("Playback too short for this song (" + playedTime / 1000
                + " secs), not submitted to LastFM");
          }
        }
      }.start();
    }
  }

  /**
   * Submit the cache if it exists.
   */
  public void submitCache() {
    try {
      if (!Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
        service.submitCache();
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }
}
