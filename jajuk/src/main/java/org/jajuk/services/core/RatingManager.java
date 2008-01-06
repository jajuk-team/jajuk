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

package org.jajuk.services.core;

import org.jajuk.Main;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
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
public class RatingManager extends Thread implements ITechnicalStrings {

  private static RatingManager self;

  /**
   * Flag the fact a rate has change for a track, used by bestof view refresh
   * for perfs
   */
  private static boolean bRateHasChanged = true;

  private RatingManager() {
    setPriority(Thread.MIN_PRIORITY);
  }

  public static RatingManager getInstance() {
    if (self == null) {
      self = new RatingManager();
    }
    return self;
  }

  public void run() {
    while (!Main.bExiting) {
      // Computes every 10 mins, until jajuk ends
      try {
        Thread.sleep(600000);
      } catch (InterruptedException e) {
        Log.error(e);
      }
      if (bRateHasChanged) {
        // refresh to update rates
        ObservationManager.notify(new Event(EventSubject.EVENT_RATE_CHANGED));
        bRateHasChanged = false;
      }

    }
  }

  /**
   * @return Returns the bRateHasChanged.
   */
  public static boolean hasRateChanged() {
    return bRateHasChanged;
  }

  /**
   * @param rateHasChanged
   *          The bRateHasChanged to set.
   */
  public static void setRateHasChanged(boolean rateHasChanged) {
    bRateHasChanged = rateHasChanged;
  }

}
