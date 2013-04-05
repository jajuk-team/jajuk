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
import java.util.HashSet;
import java.util.Set;

import net.miginfocom.layout.LinkHandler;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.Observer;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.bookmark.HistoryItem;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.tags.Tag;
import org.jajuk.services.webradio.CustomRadiosPersistenceHelper;
import org.jajuk.services.webradio.PresetRadiosPersistenceHelper;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.log.Log;

/**
 * This thread is responsible for various cleanups during jajuk execution
 * <p>
 * Singleton
 * <p>
 */
public final class CleanupService extends Thread implements Observer {
  private static CleanupService self = new CleanupService();
  private static String lastWebRadioCheckSum;
  private static String lastHistoryCheckSum;
  private static final int DELAY_BETWEEN_CHECKS_MS = 5000;

  /**
   * Instantiates a new rating manager.
   */
  private CleanupService() {
    // set thread name
    super("Cleanup service Thread");
    setPriority(Thread.MIN_PRIORITY);
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
        Thread.sleep(Const.AUTO_COMMIT_DELAY);
        Log.debug("Cleanup");
        // workaround to free space in MigLayout
        // see http://migcalendar.com/forum/viewtopic.php?f=8&t=3236&p=7012
        LinkHandler.getValue("", "", 1); // simulated read
        // Clear the tag cache to avoid growing memory usage over time
        Tag.clearCache();
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  private void commitWebradiosIfRequired() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (WebRadio radio : WebRadioManager.getInstance().getWebRadios()) {
      sb.append(radio.toString());
    }
    String checksum = MD5Processor.hash(sb.toString());
    if (!checksum.equals(lastWebRadioCheckSum)) {
      // Commit webradios
      CustomRadiosPersistenceHelper.commit();
      PresetRadiosPersistenceHelper.commit();
      lastWebRadioCheckSum = checksum;
    }
  }

  private void commitHistoryIfRequired() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (HistoryItem item : History.getInstance().getItems()) {
      sb.append(item.toString());
    }
    String checksum = MD5Processor.hash(sb.toString());
    if (!checksum.equals(lastHistoryCheckSum)) {
      History.commit();
      lastHistoryCheckSum = checksum;
    }
  }

  /**
   * Gets the single instance of RatingManager.
   * 
   * @return single instance of RatingManager
   */
  public static CleanupService getInstance() {
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
