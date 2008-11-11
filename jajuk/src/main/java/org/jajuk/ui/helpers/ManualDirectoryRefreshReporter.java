/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.helpers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.ui.wizard.RefreshDialog;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Refresh reporter on directories with GUI special operations
 */
public class ManualDirectoryRefreshReporter extends RefreshReporter {

  // Refresh dialog
  private RefreshDialog rdialog;

  private int progress;

  public ManualDirectoryRefreshReporter(Device device) {
    super(device);
  }

  public void startup() {
    super.startup();
  }

  public void reset() {
    super.reset();
  }

  public void cleanupDone() {
    Log.debug("Cleanup done");
  }

  @SuppressWarnings("unused")
  public void updateState(Directory dir) {
     // Intentionnal NOP
  }

  public void done() {
    long refreshTime = System.currentTimeMillis() - lRefreshDateStart;
    String message = buildFinalMessage(refreshTime);
    Log.debug(message);
    reset();
    // Display end of refresh message with stats
    Messages.showInfoMessage(message);
  }

  /**
   * This timer limit dialog title changes (this can have side effect on
   * performances or other in some window managers. Too many window title change
   * causes others menu bar items freezes under KDE for ie)
   */
  Timer updateDialogTitle = new Timer(500, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      String sTitle = Messages.getString("RefreshDialog.2") + " " + device.getName() + " ("
          + progress + " %)";
      if (!sTitle.equals(rdialog.getTitle())) {
        rdialog.setTitle(sTitle);
      }
    }

  });

}
