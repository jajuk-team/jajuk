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

package org.jajuk.ui.helpers;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.log.Log;

/**
 * Provides devices refresh report features <br>
 * Is responsible to manage various UI items changed during refreshing
 */
public class RefreshReporter {

  protected Device device;

  protected int dirTotal;

  protected int dirCount;

  /** Actual refresh date start* */
  protected long lRefreshDateStart;

  /** Number of new files found during refresh for stats */
  protected int iNbNewFiles;

  /** Number of corrupted files found during refresh for stats */
  protected int iNbCorruptedFiles;

  public RefreshReporter(Device device) {
    this.device = device;
  }

  public void startup() {
    // reset all values as this object is reused
    reset();
  }

  /**
   * Reset all values
   */
  protected void reset() {
    this.dirTotal = -1;
    this.dirCount = 0;
    this.iNbNewFiles = 0;
    this.iNbCorruptedFiles = 0;
    this.lRefreshDateStart = System.currentTimeMillis();
    ReadOnlyIterator<Directory> dirs = DirectoryManager.getInstance().getDirectoriesIterator();
    while (dirs.hasNext()) {
      if (dirs.next().getDevice().equals(device)) {
        dirTotal++;
      }
    }
    // To avoid "freezing" at 100% if new files have been added since last
    // refresh, take
    // 5 % of new files
    dirTotal *= 1.05;
  }

  public void notifyCorruptedFile() {
    iNbCorruptedFiles++;
  }

  public void notifyNewFile() {
    iNbNewFiles++;
  }

  public void refreshStarted() {
    lRefreshDateStart = System.currentTimeMillis();
  }

  protected String buildFinalMessage(long time) {
    StringBuilder sbOut = new StringBuilder("[").append(device.getName()).append(
        Messages.getString("Device.25"))
        .append(((time < 1000) ? time + " ms" : time / 1000 + " s")).append(" - ").append(
            iNbNewFiles).append(Messages.getString("Device.27"));
    if (iNbCorruptedFiles > 0) {
      sbOut.append(" - ").append(iNbCorruptedFiles).append(Messages.getString("Device.43"));
    }
    return sbOut.toString();
  }

  /**
   * Callback method called at the end of the refresh
   */
  public void done() {
    long refreshTime = System.currentTimeMillis() - lRefreshDateStart;
    String message = buildFinalMessage(refreshTime);
    Log.debug(message);
    reset();
  }

  /**
   * Callback method when old items cleanup is done. This method can be
   * overwritten for specific behaviors
   */
  public void cleanupDone() {
    Log.debug("Cleanup done");
  }

  /**
   * Callback method when an update state is required. Can be overwritten for
   * specific behaviors
   * @param dir 
   */
  public void updateState(Directory dir) {
    // Intentionnal NOP
  }

}
