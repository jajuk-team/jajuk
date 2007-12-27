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

package org.jajuk.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.ui.wizard.RefreshDialog;
import org.jajuk.util.log.Log;

/**
 * Provides devices refresh report features Is responsible to manage various UI
 * items changed during refreshing
 */
public class RefreshReporter {

  private Device device;

  // Refresh dialog
  private RefreshDialog rdialog;

  private int dirTotal = 0;

  private int dirCount = 0;

  /** Manual refresh date start* */
  private long lDateStart;

  /** Actual refresh date start* */
  private long lRefreshDateStart;

  /** Number of new files found during refresh for stats */
  public int iNbNewFiles;

  /** Number of corrupted files found during refresh for stats */
  public int iNbCorruptedFiles;

  public RefreshReporter(Device device) {
    this.device = device;
  }

  public void startup() {
    rdialog = new RefreshDialog();
    lDateStart = System.currentTimeMillis();
    rdialog.setTitle(Messages.getString("RefreshDialog.2") + " " + device.getName());
    // Computes the number of directories
    rdialog.setAction(Messages.getString("RefreshDialog.0"), IconLoader.ICON_INFO);
    // Count directories, takes a while, do not execute in AWT thread
    // We add 1 directory because we have to keep into account the root
    // directory
    dirTotal = Util.countDirectories(device.getFio()) + 1;
    rdialog.setAction(Messages.getString("RefreshDialog.3"), IconLoader.ICON_INFO);
    rdialog.setProgress(10);
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

  /**
   * Display a debug trace when actual refresh is done (note that automatic
   * refreshes only use this method)
   * 
   */
  public void refreshDone() {
    long refreshTime = System.currentTimeMillis() - lRefreshDateStart;
    String message = buildFinalMessage(refreshTime);
    Log.debug(message);
  }

  public void cleanupDone() {
    // Cleanup represents about 20% of the total workload
    rdialog.setProgress(20);
    rdialog.setAction(Messages.getString("RefreshDialog.1"), IconLoader.ICON_REFRESH);
    updateDialogTitle.start();
  }

  public void updateState(Directory dir) {
    if (rdialog != null) {
      rdialog.setRefreshing(new StringBuilder(Messages.getString("Device.44")).append(' ').append(
          dir.getRelativePath()).toString());
      int progress = 30 + (int) (70 * (float) dirCount / dirTotal);
      rdialog.setProgress(progress);
    }
    dirCount++;
  }

  private String buildFinalMessage(long time) {
    StringBuilder sbOut = new StringBuilder("[").append(device.getName()).append(
        Messages.getString("Device.25"))
        .append(((time < 1000) ? time + " ms" : time / 1000 + " s")).append(" - ").append(
            iNbNewFiles).append(Messages.getString("Device.27"));
    if (iNbCorruptedFiles > 0) {
      sbOut.append(" - ").append(iNbCorruptedFiles).append(Messages.getString("Device.43"));
    }
    return sbOut.toString();
  }

  public void done() {
    // Close refresh dialog
    rdialog.dispose();
    // Close title timer
    updateDialogTitle.stop();
    // Display end of refresh message with stats
    String message = buildFinalMessage(System.currentTimeMillis() - lDateStart);
    Messages.showInfoMessage(message);
  }

  /**
   * This timer limit dialog title changes (this can have side effect on
   * performances or other in some window managers. Too many window title change
   * causes others menu bar items freezes under KDE for ie)
   */
  Timer updateDialogTitle = new Timer(1000, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      int progress = 30 + (int) (70 * (float) dirCount / dirTotal);
      String sTitle = Messages.getString("RefreshDialog.2") + " " + device.getName() + " ("
          + progress + " %)";
      if (!sTitle.equals(rdialog.getTitle())) {
        rdialog.setTitle(sTitle);
      }
    }

  });
}
