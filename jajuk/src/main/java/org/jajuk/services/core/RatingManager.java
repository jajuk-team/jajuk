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
package org.jajuk.services.core;

import java.util.HashSet;
import java.util.Set;

import org.jajuk.base.FileManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.log.Log;

/**
 * This thread is responsible for refreshing elements related to ratings (UI
 * refresh in tables, bestof files computations...)
 * <p>
 * It exists for performance reasons
 * </p>
 * <p>
 * Singleton
 * <p>
 */
public final class RatingManager extends Thread implements Observer {
  private static RatingManager self = new RatingManager();
  /** Flag the fact a rate has change for a track, used by bestof view refresh for perfs. */
  private static boolean bRateHasChanged = true;
  /** Max rate. */
  private static long lMaxPlaycount = 0l;

  /**
   * Instantiates a new rating manager.
   */
  private RatingManager() {
    // set thread name
    super("Rating Manager Thread");
    setPriority(Thread.MIN_PRIORITY);
    // Look for events
    ObservationManager.register(this);
  }

  /**
   * Gets the single instance of RatingManager.
   * 
   * @return single instance of RatingManager
   */
  public static RatingManager getInstance() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    while (!ExitService.isExiting()) {
      // Computes every 10 mins, until jajuk ends
      try {
        Thread.sleep(600000);
        // Computes bestof
        FileManager.getInstance().refreshBestOfFiles();
      } catch (InterruptedException e) {
        Log.error(e);
      }
      if (bRateHasChanged) {
        // refresh to update rates
        ObservationManager.notify(new JajukEvent(JajukEvents.RATE_CHANGED));
        bRateHasChanged = false;
      }
    }
  }

  /**
   * Gets the max playcount.
   * 
   * @return maximum rating between all tracks
   */
  public static long getMaxPlaycount() {
    return lMaxPlaycount;
  }

  /**
   * Return a valid [0;100] rating for given preference
   * @param preference
   * @return
   */
  public static long getRateForPreference(long preference) {
    return Math.round((16.6 * preference + 50));
  }

  /**
   * Set max playcount.
   * 
   * @param value the playcount value
   */
  public static void setMaxPlaycount(long value) {
    lMaxPlaycount = value;
    // Means that the playcount has been reset so recompute them
    if (lMaxPlaycount == 0) {
      // Computes bestof
      FileManager.getInstance().refreshBestOfFiles();
    }
  }

  /**
   * Checks for rate changed.
   * 
   * @return Returns the bRateHasChanged.
   */
  public static boolean hasRateChanged() {
    return bRateHasChanged;
  }

  /**
   * Sets the rate has changed.
   * 
   * @param rateHasChanged The bRateHasChanged to set.
   */
  public static void setRateHasChanged(boolean rateHasChanged) {
    bRateHasChanged = rateHasChanged;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.RATE_RESET);
    eventSubjectSet.add(JajukEvents.PREFERENCES_RESET);
    eventSubjectSet.add(JajukEvents.RATING_MODE_CHANGED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.Event)
   */
  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    // Reset rate and total play time (automatic part of rating system)
    if (subject.equals(JajukEvents.RATE_RESET)) {
      // Reset playcount
      setMaxPlaycount(0);
      /*
       * Reset rates, use a track list, not an iterator as this can be called during a refresh and
       * cause a ConcurrentModificationException
       */
      for (Track track : TrackManager.getInstance().getTracks()) {
        track.setProperty(Const.XML_TRACK_RATE, 0l);
        track.setProperty(Const.XML_TRACK_TOTAL_PLAYTIME, 0l);
        track.setHits(0l);
      }
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
      // Force suggestion view refresh. Not that the suggestion view doesn't
      // subscribe to EVENT_RATE_RESET event directly because we don't ensure
      // that the view will trap the event only after this class
      ObservationManager.notify(new JajukEvent(JajukEvents.SUGGESTIONS_REFRESH));
      // Computes bestof
      FileManager.getInstance().refreshBestOfFiles();
      InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.252"),
          InformationJPanel.MessageType.INFORMATIVE);
    }
    // Reset the manual part of the rating system : preferences
    else if (subject.equals(JajukEvents.PREFERENCES_RESET)) {
      // Reset preferences
      ReadOnlyIterator<Track> it = TrackManager.getInstance().getTracksIterator();
      while (it.hasNext()) {
        Track track = it.next();
        track.setProperty(Const.XML_TRACK_PREFERENCE, 0l);
        track.updateRate();
      }
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
      // Force suggestion view refresh. Not that the suggestion view doesn't
      // subscribe to EVENt_RATE_RESET event directly because we don't ensure
      // that the view will trap the event only after this class
      ObservationManager.notify(new JajukEvent(JajukEvents.SUGGESTIONS_REFRESH));
      // Computes bestof
      FileManager.getInstance().refreshBestOfFiles();
      InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.253"),
          InformationJPanel.MessageType.INFORMATIVE);
    } else if (subject == JajukEvents.RATING_MODE_CHANGED) {
      // Update rate in case of manual/auto ratings switch
      for (Track track : TrackManager.getInstance().getTracks()) {
        track.updateRate();
      }
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }
  }
}
