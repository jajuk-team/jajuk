/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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

import java.util.HashSet;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.lastfm.model.FullSubmissionData;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

import org.jajuk.services.lastfm.scrobble.ScrobblerException;

/**
 * LastFM Manager, handle file launch events to submit informations among others
 * things.
 * <p>
 * singleton
 * </p>
 */
public final class LastFmManager implements Observer, Const {
  /** Self instance. */
  private static final LastFmManager self = new LastFmManager();
  /** Lastfm service. */
  private final LastFmService service;

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
    Set<JajukEvents> eventSubjectSet = new HashSet<>();
    eventSubjectSet.add(JajukEvents.FILE_FINISHED);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  @Override
  public void update(final JajukEvent event) {
    if (!Conf.getBoolean(Const.CONF_LASTFM_AUDIOSCROBBLER_ENABLE)
            || Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      // Do nothing if scrobble or network is disabled
      return;
    }
    final JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.FILE_FINISHED)) {
      final File file = (File) event.getDetails().get(Const.DETAIL_CURRENT_FILE);
      if (!file.getTrack().getBooleanValue(XML_TRACK_SCROBBLE)) {
        Log.debug("Track scrobble property unset, not submitted to last.fm : "
                + file.getTrack().getID());
        return;
      }
      new Thread("LastFM Scrobble Thread") {
        @Override
        public void run() {
          long playedTime = (Long) event.getDetails().get(Const.DETAIL_CONTENT);
          // Last.FM rule : only submit >= 30secs playbacks
          if (playedTime >= 30000) {
            FullSubmissionData data = null;
            try {
              //service.submit(file.getTrack(), playedTime);
              data = new FullSubmissionData(file.getTrack().getAlbumArtistOrArtist(),
                      file.getTrack().getTitle(),
                      file.getTrack().getAlbum().getTitle(),
                      (int) file.getTrack().getDuration(),
                      (int) file.getTrack().getDiscNumber(),
                      0);

              service.addSubmission(data);
              service.submitCache();
            } catch (ScrobblerException e) {
              if (!e.getMessage().contains("Network")) {
                // To avoid infinite list in case of technical issue.
                try {
                  service.removeSubmission(data);
                } catch (ScrobblerException ex) {
                  Log.warn("Unable to remove submission from cache : " + ex.getMessage());
                }
              }
              Log.error(e);
            }
          } else {
            Log.info("Playback too short for this song (" + playedTime / 1000
                    + " secs), not submitted to LastFM");
          }
        }
      }.start();
    } else if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      final String sFileId = (String) event.getDetails().get(Const.DETAIL_CURRENT_FILE_ID);
      // check the ID maps an existing file
      final File file = FileManager.getInstance().getFileByID(sFileId);
      if (file == null) {
        return;
      }
      //pDetails.put(Const.DETAIL_CURRENT_DATE, System.currentTimeMillis());
      if (!file.getTrack().getBooleanValue(XML_TRACK_SCROBBLE)) {
        Log.debug("Track scrobble property unset, not submitted to last.fm : "
                + file.getTrack().getID());
        return;
      }
      new Thread("LastFM UpdateNowPlaying Thread") {
        @Override
        public void run() {
          long playedTime = (Long) event.getDetails().get(Const.DETAIL_CURRENT_DATE);
          try {
            service.submit(file.getTrack(), playedTime);
          } catch (ScrobblerException e) {
            Log.error(e);
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
