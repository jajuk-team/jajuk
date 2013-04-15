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
package org.jajuk.services.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jajuk.base.Collection;
import org.jajuk.base.DeviceManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.bookmark.HistoryItem;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.CustomRadiosPersistenceHelper;
import org.jajuk.services.webradio.PresetRadiosPersistenceHelper;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.log.Log;

/**
 * This thread is responsible for commiting configuration or collection files on events. 
 * This allows to save files during Jajuk running and not when exiting the app as before. 
 * Saving on exit is problematic even using an exit hook because save can be partial on fast computers.
 * <p>
 * It is sometimes difficult to get clear events to check to so we also start a differential check 
 * on a regular basis through a thread
 * </p>
 * <p>
 * Singleton
 * <p>
 */
public final class PersistenceService extends Thread implements Observer {
  public enum Urgency {
    HIGH, MEDIUM, LOW
  }

  private static PersistenceService self = new PersistenceService();
  private String lastWebRadioCheckSum;
  private String lastHistoryCheckSum;
  private static final int HEART_BEAT_MS = 1000;
  private static final int DELAY_HIGH_URGENCY_BEATS = 5;
  private static final int DELAY_MEDIUM_URGENCY_BEATS = 15;
  private static final int DELAY_LOW_URGENCY_BEATS = 60 * HEART_BEAT_MS;
  /** Collection change flag **/
  private Map<Urgency, Boolean> collectionChanged = new HashMap<Urgency, Boolean>(3);
  private boolean started = false;

  /**
   * @return the started
   */
  public boolean isStarted() {
    return this.started;
  }

  /**
   * Inform the persister service that the collection should be commited with the given urgency
   * @param urgency the urgency for the collection to be commited
   */
  public void tagCollectionChanged(Urgency urgency) {
    collectionChanged.put(urgency, true);
  }

  /**
   * Instantiates a new rating manager.
   */
  private PersistenceService() {
    // set thread name
    super("Persistence Manager Thread");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    init();
    int comp = 1;
    while (!ExitService.isExiting()) {
      try {
        Thread.sleep(HEART_BEAT_MS);
        if (comp % DELAY_HIGH_URGENCY_BEATS == 0) {
          performHighUrgencyActions();
        }
        if (comp % DELAY_MEDIUM_URGENCY_BEATS == 0) {
          performMediumUrgencyActions();
        }
        if (comp % DELAY_LOW_URGENCY_BEATS == 0) {
          performLowUrgencyActions();
        }
        comp++;
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  private void init() {
    // Store current history to avoid commiting it at startup. 
    // Note however that the history will be changed (thus commited) 
    // if jajuk is in last-track restart mode because this mode changes the item date at next session startup 
    this.lastHistoryCheckSum = getHistoryChecksum();
    this.lastWebRadioCheckSum = getWebradiosChecksum();
    collectionChanged.put(Urgency.LOW, false);
    collectionChanged.put(Urgency.MEDIUM, false);
    collectionChanged.put(Urgency.HIGH, false);
    setPriority(Thread.MAX_PRIORITY);
    // Look for events
    ObservationManager.register(this);
    started = true;
  }

  private void performHighUrgencyActions() throws Exception {
    commitHistoryIfRequired();
    if (collectionChanged.get(Urgency.HIGH)) {
      try {
        commitCollectionIfRequired();
      } finally {
        collectionChanged.put(Urgency.HIGH, false);
      }
    }
  }

  private void performMediumUrgencyActions() throws Exception {
    commitWebradiosIfRequired();
    if (collectionChanged.get(Urgency.MEDIUM)) {
      try {
        commitCollectionIfRequired();
      } finally {
        collectionChanged.put(Urgency.MEDIUM, false);
      }
    }
  }

  private void performLowUrgencyActions() throws Exception {
    if (collectionChanged.get(Urgency.LOW)) {
      try {
        commitCollectionIfRequired();
      } finally {
        collectionChanged.put(Urgency.LOW, false);
      }
    }
  }

  private void commitCollectionIfRequired() throws IOException {
    // Commit collection if not still refreshing
    if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
      Collection.commit(SessionService.getConfFileByPath(Const.FILE_COLLECTION_EXIT));
    }
  }

  private void commitWebradiosIfRequired() throws IOException {
    String checksum = getWebradiosChecksum();
    if (!checksum.equals(lastWebRadioCheckSum)) {
      // Commit webradios
      CustomRadiosPersistenceHelper.commit();
      PresetRadiosPersistenceHelper.commit();
      lastWebRadioCheckSum = checksum;
    }
  }

  private String getHistoryChecksum() {
    StringBuilder sb = new StringBuilder();
    for (HistoryItem item : History.getInstance().getItems()) {
      sb.append(item.toString());
    }
    String checksum = MD5Processor.hash(sb.toString());
    return checksum;
  }

  private String getWebradiosChecksum() {
    StringBuilder sb = new StringBuilder();
    List<WebRadio> radios = WebRadioManager.getInstance().getWebRadios();
    // Sort webradios because collections sorting is done asynchronously for startup 
    // speed reasons and we need a stable radios order for our checksum
    Collections.sort(radios);
    for (WebRadio radio : radios) {
      sb.append(radio.toString());
    }
    String checksum = MD5Processor.hash(sb.toString());
    return checksum;
  }

  private void commitHistoryIfRequired() throws IOException {
    String checksum = getHistoryChecksum();
    if (!checksum.equals(lastHistoryCheckSum)) {
      History.commit();
      this.lastHistoryCheckSum = checksum;
    }
  }

  /**
   * Gets the single instance of RatingManager.
   * 
   * @return single instance of RatingManager
   */
  public static PersistenceService getInstance() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.Event)
   */
  @Override
  public void update(JajukEvent event) {
    try {
      JajukEvents subject = event.getSubject();
      if (subject == JajukEvents.FILE_LAUNCHED) {
        // Store current FIFO for next session
        QueueModel.commit();
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }
}
