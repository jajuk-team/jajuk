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

import net.miginfocom.layout.LinkHandler;

import org.jajuk.services.tags.Tag;
import org.jajuk.util.log.Log;

/**
 * This thread is responsible for various cleanups during jajuk execution
 * <p>
 * Singleton
 * <p>
 */
public final class CleanupService extends Thread {
  private static CleanupService self = new CleanupService();
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
        Thread.sleep(DELAY_BETWEEN_CHECKS_MS);
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

  /**
   * Gets the single instance of RatingManager.
   * 
   * @return single instance of RatingManager
   */
  public static CleanupService getInstance() {
    return self;
  }
}
