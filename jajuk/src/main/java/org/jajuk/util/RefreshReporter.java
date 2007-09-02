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

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.wizard.RefreshDialog;
import org.jajuk.util.log.Log;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

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

	private final int deviceURLSize;

	private long lDateStart;

	/** Number of new files found during refresh for stats */
	public int iNbNewFiles;

	/** Number of corrupted files found during refresh for stats */
	public int iNbCorruptedFiles;

	/** Refresh message */
	private String sFinalMessage = "";

	public RefreshReporter(Device device) {
		this.device = device;
		this.deviceURLSize = device.getUrl().length();
	}

	public void startup() {
		rdialog = new RefreshDialog();
		lDateStart = System.currentTimeMillis();
		rdialog.setTitle(Messages.getString("RefreshDialog.2") + " " + device.getName());
		// Computes the number of directories
		rdialog.setAction(Messages.getString("RefreshDialog.0"), IconLoader.ICON_INFO);
		dirTotal = Util.countDirectories(device.getFio());
		rdialog.setAction(Messages.getString("RefreshDialog.3"), IconLoader.ICON_INFO);
		rdialog.setProgress(10);
		updateDialogTitle.start();
	}

	public void notifyCorruptedFile() {
		iNbCorruptedFiles++;
	}

	public void notifyNewFile() {
		iNbNewFiles++;
	}

	public void cleanupDone() {
		// Cleanup represents about 20% of the total workload
		rdialog.setProgress(20);
		rdialog.setAction(Messages.getString("RefreshDialog.1"), IconLoader.ICON_REFRESH);
	}

	public void updateState(Directory dir) {
		rdialog.setRefreshing(new StringBuffer(Messages.getString("Device.44")).append(' ').append(
				dir.getRelativePath()).toString());
		int progress = 30 + (int) (70 * (float) dirCount / dirTotal);
		rdialog.setProgress(progress);
		dirCount++;
	}

	public void done() {
		// Close refresh dialog
		rdialog.dispose();
		//Close title timer
		updateDialogTitle.stop();
		// Display end of refresh message with stats
		lDateStart = System.currentTimeMillis() - lDateStart;
		StringBuffer sbOut = new StringBuffer("[").append(device.getName()).append(
				Messages.getString("Device.25")).append(
				((lDateStart < 1000) ? lDateStart + " ms" : lDateStart / 1000 + " s"))
				.append(" - ").append(iNbNewFiles).append(Messages.getString("Device.27"));
		if (iNbCorruptedFiles > 0) {
			sbOut.append(" - ").append(iNbCorruptedFiles).append(Messages.getString("Device.43"));
		}
		sFinalMessage = sbOut.toString();
		Log.debug(sFinalMessage);
		// Display a message in information panel
		Messages.showInfoMessage(sFinalMessage);
	}

	/**
	 * This timer limit dialog title changes (this can have side effect on
	 * performances or other in some window managers. Too many window title
	 * change causes others menu bar items freezes under KDE for ie)
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
