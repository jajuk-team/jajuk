/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.services.lastfm;

import ext.services.lastfm.LastFmService;
import ext.services.lastfm.ScrobblerException;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
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
 * things.
 * <p>
 * singleton
 * </p>
 */
public final class LastFmManager implements Observer, Const {

  /** Self instance. */
  private static LastFmManager self;

  /** Lastfm service. */
  private LastFmService service;

  /**
   * Instantiates a new last fm manager.
   */
  private LastFmManager() {
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // Display an hideable message to user if audioscrobber is disable
    // Show this message only one time by jajuk session
    if (!Conf.getBoolean(Const.CONF_LASTFM_ENABLE)
    // don't show this message if first jajuk launch: already too many
        // popups.
        && !UpgradeManager.isFirstSession()
        // don't show neither if last.fm login is already provided but disabled
        && StringUtils.isBlank(Conf.getString(Const.CONF_LASTFM_USER))) {
      Messages.showHideableWarningMessage(Messages.getString("LastFmManager.0"),
          CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED);
    }
    // Create the service
    service = LastFmService.getInstance();

  }

  /**
   * Gets the single instance of LastFmManager.
   * 
   * @return single instance of LastFmManager
   */
  public static LastFmManager getInstance() {
    if (self == null) {
      self = new LastFmManager();
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

  /**
   * Configure.
   * DOCUMENT_ME
   */
  public void configure() {
    service.setPassword(UtilString.rot13(Conf.getString(Const.CONF_LASTFM_PASSWORD)));
    service.setUser(Conf.getString(Const.CONF_LASTFM_USER));
    service.setProxy(DownloadManager.getProxy());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(final JajukEvent event) {
    if (JajukEvents.FILE_FINISHED == event.getSubject()
        && !Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      new Thread("LastFM Update Thread") {
        @Override
        public void run() {
          if (Conf.getBoolean(Const.CONF_LASTFM_ENABLE)) {
            File file = (File) event.getDetails().get(Const.DETAIL_CURRENT_FILE);
            long playedTime = file.getTrack().getDuration();
            // If we are in intro mode, computes actually listened
            // time
            if (Conf.getBoolean(Const.CONF_STATE_INTRO)) {
              playedTime = (playedTime * Conf.getInt(Const.CONF_OPTIONS_INTRO_BEGIN) / 100)
                  - Conf.getInt(Const.CONF_OPTIONS_INTRO_BEGIN);
            }
            try {
              service.submit(file.getTrack(), playedTime);
            } catch (ScrobblerException e) {
              Log.error(e);
            }
          }

        }
      }.start();
    }
  }

  /**
   * Submit the cache if it exists
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
