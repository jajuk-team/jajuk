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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.wizard.RefreshDialog;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.log.Log;

/**
 * Provides devices refresh report features Is responsible to manage various UI
 * items changed during refreshing
 */
public class RefreshReporter {

  private Device device;

  // Refresh dialog
  private RefreshDialog rdialog;

  private int progress;

  private int dirTotal;

  private int dirCount;

  /** Manual refresh date start* */
  private long lDateStart;

  /** Actual refresh date start* */
  private long lRefreshDateStart;

  /** Number of new files found during refresh for stats */
  private int iNbNewFiles;

  /** Number of corrupted files found during refresh for stats */
  private int iNbCorruptedFiles;

  public RefreshReporter(Device device) {
    this.device = device;
    // Keep this date capture: it is used by directories refresh that don't call
    // the startup() method
    lDateStart = System.currentTimeMillis();
  }

  public void startup() {
    // reset all values as this object is reused
    this.progress = 0;
    this.dirTotal = -1;
    this.dirCount = 0;
    this.iNbNewFiles = 0;
    this.iNbCorruptedFiles = 0;
    this.lDateStart = System.currentTimeMillis();
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

    // if <0 directories count -> the progress bar is in indeterminate state
    this.rdialog = new RefreshDialog((dirTotal < 0));
    this.rdialog.setTitle(Messages.getString("RefreshDialog.2") + " " + device.getName());
    // Computes the number of directories
    this.rdialog.setAction(Messages.getString("RefreshDialog.0"), IconLoader
        .getIcon(JajukIcons.INFO));
    // Count directories, takes a while, do not execute in AWT thread
    // If we already refreshed the device, use previous size as best
    // guess. If it is the first refresh don't count (user reported that it is
    // too long in some cases), but display a default slider

    this.rdialog.setAction(Messages.getString("RefreshDialog.3"), IconLoader
        .getIcon(JajukIcons.INFO));
    this.rdialog.setProgress(10);
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
    rdialog
        .setAction(Messages.getString("RefreshDialog.1"), IconLoader.getIcon(JajukIcons.REFRESH));
    // Update counter only if final directory count is known
    if (dirTotal > 0) {
      updateDialogTitle.start();
    }
  }

  public void updateState(Directory dir) {
    if (rdialog != null) {
      rdialog.setRefreshing(new StringBuilder(Messages.getString("Device.44")).append(' ').append(
          dir.getRelativePath()).toString());
      progress = 30 + (int) (70 * (float) dirCount / dirTotal);
      if (progress > 100) {
        progress = 100;
      }
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
    // rdialog is null for directories refresh
    if (rdialog != null) {
      // Close refresh dialog
      rdialog.dispose();
      // Close title timer
      updateDialogTitle.stop();
    }
    // Display end of refresh message with stats
    String message = buildFinalMessage(System.currentTimeMillis() - lDateStart);
    Messages.showInfoMessage(message);
    InformationJPanel.getInstance().setMessage(message, 1);
  }

  /**
   * This timer limit dialog title changes (this can have side effect on
   * performances or other in some window managers. Too many window title change
   * causes others menu bar items freezes under KDE for ie)
   */
  Timer updateDialogTitle = new Timer(1000, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      String sTitle = Messages.getString("RefreshDialog.2") + " " + device.getName() + " ("
          + progress + " %)";
      if (!sTitle.equals(rdialog.getTitle())) {
        rdialog.setTitle(sTitle);
      }
    }

  });
}
