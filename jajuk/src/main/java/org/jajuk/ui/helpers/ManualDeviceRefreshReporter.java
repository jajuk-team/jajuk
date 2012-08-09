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
package org.jajuk.ui.helpers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.ui.wizard.RefreshDialog;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Refresh reporter with GUI special operations.
 */
public class ManualDeviceRefreshReporter extends RefreshReporter {
  // Refresh dialog
  private RefreshDialog rdialog;
  private int progress;

  /**
   * Instantiates a new manual device refresh reporter.
   * 
   * @param device 
   */
  public ManualDeviceRefreshReporter(Device device) {
    super(device);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.RefreshReporter#startup()
   */
  @Override
  public void startup() {
    super.startup();
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          // if <0 directories count -> the progress bar is in indeterminate
          // state
          rdialog = new RefreshDialog((dirTotal < 0), Messages.getString("RefreshDialog.2") + " "
              + device.getName());
          rdialog.setAction(Messages.getString("RefreshDialog.3"),
              IconLoader.getIcon(JajukIcons.INFO));
        }
      });
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.RefreshReporter#reset()
   */
  @Override
  public void reset() {
    super.reset();
    this.progress = 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.RefreshReporter#cleanupDone()
   */
  @Override
  public void cleanupDone() {
    // We estimate that cleanup represents about 20% of the total workload
    rdialog.setProgress(20);
    rdialog
        .setAction(Messages.getString("RefreshDialog.1"), IconLoader.getIcon(JajukIcons.REFRESH));
    // Update counter only if final directory count is known
    if (dirTotal > 0) {
      updateDialogTitle.start();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.jajuk.ui.helpers.RefreshReporter#updateState(org.jajuk.base.Directory)
   */
  @Override
  public void updateState(Directory dir) {
    if (rdialog != null) {
      rdialog.setRefreshing(new StringBuilder(Messages.getString("Device.44")).append(' ')
          .append(dir.getRelativePath()).toString());
      progress = 30 + (int) (70 * (float) dirCount / dirTotal);
      if (progress > 100) {
        progress = 100;
      }
      rdialog.setProgress(progress);
    }
    dirCount++;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.RefreshReporter#done()
   */
  @Override
  public void done() {
    done(true);
  }

  /**
   * Done.
   * 
   * @param showInfoMessageDuration show info message with total duration when finished
   */
  public void done(boolean showInfoMessageDuration) {
    long refreshTime = System.currentTimeMillis() - lRefreshDateStart;
    String message = buildFinalMessage(refreshTime);
    Log.debug(message);
    reset();
    // Close refresh dialog
    rdialog.dispose();
    // Close title timer
    updateDialogTitle.stop();
    // Display end of refresh message with stats
    if (showInfoMessageDuration) {
      Messages.showInfoMessage(message);
    }
  }

  /** This timer limit dialog title changes (this can have side effect on performances or other in some window managers. Too many window title change causes others menu bar items freezes under KDE for ie) */
  Timer updateDialogTitle = new Timer(1000, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      String sTitle = Messages.getString("RefreshDialog.2") + " " + device.getName() + " ("
          + progress + " %)";
      if (!sTitle.equals(rdialog.getTitle())) {
        rdialog.setTitle(sTitle);
      }
    }
  });
}
