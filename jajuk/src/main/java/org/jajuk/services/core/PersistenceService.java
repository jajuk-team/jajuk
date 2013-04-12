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
import java.util.HashSet;
import java.util.List;
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
  private static PersistenceService self = new PersistenceService();
  private String lastWebRadioCheckSum;
  private String lastHistoryCheckSum;
  private static final int DELAY_BETWEEN_CHECKS_MS = 5000;
  /** Collection change flag **/
  private static boolean collectionChanged = false;

  /**
   * @param collectionChanged the collectionChanged to set
   */
  public static void tagCollectionChanged() {
    PersistenceService.collectionChanged = true;
  }

  /**
   * Instantiates a new rating manager.
   */
  private PersistenceService() {
    // set thread name
    super("Persistence Manager Thread");
    // Store current history to avoid commiting it at startup. 
    // Note however that the history will be changed (thus commited) 
    // if jajuk is in last-track restart mode because this mode changes the item date at next session startup 
    this.lastHistoryCheckSum = getHistoryChecksum();
    this.lastWebRadioCheckSum = getWebradiosChecksum();
    setPriority(Thread.MAX_PRIORITY);
    // Look for events
    ObservationManager.register(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    while (!ExitService.isExiting()) {
      try {
        Thread.sleep(DELAY_BETWEEN_CHECKS_MS);
        try {
          commitWebradiosIfRequired();
          commitHistoryIfRequired();
          commitCollectionIfRequired();
        } catch (Exception e) {
          Log.error(e);
        }
      } catch (InterruptedException e) {
        Log.error(e);
      }
    }
  }

  private void commitCollectionIfRequired() throws IOException {
    // Commit collection if not still refreshing
    if (!DeviceManager.getInstance().isAnyDeviceRefreshing()
        && PersistenceService.collectionChanged) {
      Collection.commit(SessionService.getConfFileByPath(Const.FILE_COLLECTION_EXIT));
      PersistenceService.collectionChanged = false;
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
