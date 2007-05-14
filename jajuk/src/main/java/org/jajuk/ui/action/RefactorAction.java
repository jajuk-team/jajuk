/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.action;

import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

public class RefactorAction implements ITechnicalStrings {

	ArrayList<File> alFiles;

	String filename;
	
	public static boolean bStopAll = false;

	static String sFS = java.io.File.separator;

	public RefactorAction(ArrayList<File> al) {
		alFiles = al;
		Iterator it = alFiles.iterator();
		String sFiles = ""; //$NON-NLS-1$
		while (it.hasNext()) {
			File f = (File) it.next();
			sFiles += f.getName() + "\n"; //$NON-NLS-1$
		}
		if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_REFACTOR_FILES)) {
			int iResu = Messages
					.getChoice(
							Messages.getString("Confirmation_refactor_files") + " : \n" + sFiles, JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			if (iResu != JOptionPane.YES_OPTION) {
				//Cancel
				if (iResu == JOptionPane.CANCEL_OPTION){
					bStopAll = true;
				}
				Util.stopWaiting();
				return;
			}
		}
		new Thread() {
			public void run() {
				Util.waiting();
				refactor();
				ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
			}
		}.start();
		Util.stopWaiting();
	}

	public void refactor() {
		Iterator it = alFiles.iterator();
		String sErrors = ""; //$NON-NLS-1$
		while (it.hasNext()) {
			File fCurrent = (File) it.next();
			Track tCurrent = fCurrent.getTrack();
			try {
				filename = Util.applyPattern(fCurrent, ConfigurationManager
						.getProperty(CONF_REFACTOR_PATTERN), true);
			} catch (JajukException je) {
				sErrors += je.getMessage() + '\n';
				continue;
			}

			filename += "." + tCurrent.getType().getExtension(); //$NON-NLS-1$
			filename = filename.replace("/", sFS); //$NON-NLS-1$

			// Compute the new filename
			java.io.File fOld = fCurrent.getIO();
			String sRoot = fCurrent.getDevice().getFio().getPath();

			// Check if directories exists, and if not create them
			String sPathname = getCheckedPath(sRoot, filename);
			java.io.File fNew = new java.io.File(sPathname);
			fNew.getParentFile().mkdirs();

			// Move file and related cover but save old Directory pathname
			// for future deletion
			java.io.File fCover = tCurrent.getAlbum().getCoverFile();
			if (fCover != null) {
				fCover.renameTo(new java.io.File(fNew.getParent() + sFS + fCover.getName()));
			}
			boolean bState = false;

			if (fNew.getAbsolutePath().equalsIgnoreCase(fOld.getAbsolutePath())) {
				sErrors += fCurrent.getAbsolutePath() + " (" //$NON-NLS-1$
						+ Messages.getString("Error.160") + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (fNew.getParentFile().canWrite()) {
					bState = fOld.renameTo(fNew);
					if (!bState) {
						sErrors += fCurrent.getAbsolutePath() + " (" //$NON-NLS-1$
								+ Messages.getString("Error.154") + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					Log.debug("[Refactoring] {{" + fNew.getAbsolutePath() //$NON-NLS-1$
							+ "}} Success ? " + bState); //$NON-NLS-1$

				} else {
					sErrors += fCurrent.getAbsolutePath() + " (" //$NON-NLS-1$
							+ Messages.getString("Error.161") + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			// Register and scans new directories
			String sFirstDir = ""; //$NON-NLS-1$
			String sTest[] = sPathname.split(sRoot.replace("\\", "\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
			sFirstDir = sTest[1].split("\\" + sFS)[1]; //$NON-NLS-1$

			Directory dir = DirectoryManager.getInstance()
					.registerDirectory(
							sFirstDir,
							DirectoryManager.getInstance().getDirectoryForIO(
									fCurrent.getDevice().getFio()), fCurrent.getDevice());

			registerFile(dir);

			// See if old directory contain other files and move them
			java.io.File dOld = fOld.getParentFile();
			java.io.File[] list = dOld.listFiles(new JajukFileFilter(JajukFileFilter.NotAudioFilter
					.getInstance()));
			if (list == null) {
				DirectoryManager.getInstance().removeDirectory(fOld.getParent());
			} else if (list.length != 0) {
				for (java.io.File f : list) {
					f.renameTo(new java.io.File(fNew.getParent() + sFS + f.getName()));
				}
			} else if (list.length == 0) {
				if (dOld.delete()) {
					DirectoryManager.getInstance().removeDirectory(fOld.getParent());

				}
			}
			fCurrent.getDevice().cleanRemovedFiles();

			InformationJPanel.getInstance().setMessage(
					Messages.getString("RefactorWizard.0") + sPathname, 0); //$NON-NLS-1$
		}

		if (!sErrors.equals("")) { //$NON-NLS-1$
			Messages.showDetailedErrorMessage("147", "", sErrors); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			InformationJPanel.getInstance().setMessage(
					Messages.getString("Success"), InformationJPanel.INFORMATIVE); //$NON-NLS-1$
		}

	}

	public String getCheckedPath(String sRoot, String sPathname) {

		java.io.File fioRoot = new java.io.File(sRoot);
		java.io.File[] fioList = fioRoot.listFiles(new JajukFileFilter(
				JajukFileFilter.DirectoryFilter.getInstance()));
		String[] sPaths = sPathname.split("\\" + sFS); //$NON-NLS-1$
		String sReturn = sRoot;
		for (int i = 0; i < sPaths.length - 1; i++) {
			String sPath = sPaths[i];
			boolean bool = false;
			for (java.io.File fio : fioList) {
				String s = fio.getPath();
				if (s.equalsIgnoreCase(sReturn + sFS + sPath)) {
					sReturn += sFS + fio.getName();
					bool = true;
				}
			}
			if (bool == false) {
				sReturn += sFS + sPath;
			}
		}
		return sReturn + sFS + sPaths[sPaths.length - 1];
	}

	public void registerFile(Directory d) {

		java.io.File fList[] = d.getFio().listFiles(
				new JajukFileFilter(JajukFileFilter.DirectoryFilter.getInstance()));

		if (fList != null && fList.length != 0) {
			for (java.io.File f : fList) {
				Directory dir = DirectoryManager.getInstance().registerDirectory(f.getName(), d,
						d.getDevice());
				registerFile(dir);
			}
		} else {
			d.scan(true);
		}
	}
}
