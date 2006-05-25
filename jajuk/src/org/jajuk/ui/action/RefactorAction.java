/*
 *  Jajuk
 *  Copyright (C) 2006 Administrateur
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

package org.jajuk.ui.action;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.base.AlbumManager;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CDDBWizard;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

public class RefactorAction implements ITechnicalStrings {

	ArrayList<File> alFiles;

	String filename;

	public RefactorAction(ArrayList<File> al) {
		alFiles = al;
		Iterator it = alFiles.iterator();
		String sFiles = "";
		while (it.hasNext()) {
			File f = (File) it.next();
			sFiles += f.getName() + "\n";
		}		
		if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_REFACTOR_FILES)) {
			int iResu = Messages
					.getChoice(
							Messages.getString("Confirmation_refactor_files") + " : \n" + sFiles, JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			if (iResu != JOptionPane.YES_OPTION) {
				Util.stopWaiting();
				return;
			}
		}
		new Thread() {
			public void run() {
				refactor();
				ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));
			}
		}.start();
		Util.stopWaiting();
	}

	public void refactor() {
		Iterator it = alFiles.iterator();
		String sErrors = "";
		while (it.hasNext()) {
			File fCurrent = (File) it.next();
			Track tCurrent = fCurrent.getTrack();
			filename = ConfigurationManager.getProperty(CONF_REFACTOR_PATTERN)
					.toLowerCase();

			String sValue;
			// Check Author name
			if (filename.contains(PATTERN_ARTIST)) {
				sValue = tCurrent.getAuthor().getName2().replace("/", "-");
				if (!sValue.equalsIgnoreCase(Messages.getString("unknown"))) {
					filename = filename.replace(PATTERN_ARTIST, AuthorManager
							.format(sValue));
				} else {
					sErrors += fCurrent.getAbsolutePath() + " ("
							+ Messages.getString("Error.150") + ")\n";
					continue;
				}
			}

			// Check Style name
			if (filename.contains(PATTERN_GENRE)) {
				sValue = tCurrent.getStyle().getName2().replace("/", "-");
				if (!sValue.equalsIgnoreCase(Messages.getString("unknown"))) {
					filename = filename.replace(PATTERN_GENRE, StyleManager
							.format(sValue));
				} else {
					sErrors += fCurrent.getAbsolutePath() + " ("
							+ Messages.getString("Error.153") + ")\n";
					continue;
				}
			}

			// Check Album Name
			if (filename.contains(PATTERN_ALBUM)) {
				sValue = tCurrent.getAlbum().getName2().replace("/", "-");
				if (!sValue.equalsIgnoreCase(Messages.getString("unknown"))) {
					filename = filename.replace(PATTERN_ALBUM, AlbumManager
							.format(sValue));
				} else {
					sErrors += fCurrent.getAbsolutePath() + " ("
							+ Messages.getString("Error.149") + ")\n";
					continue;
				}
			}

			// Check Track Order
			if (filename.contains(PATTERN_TRACKORDER)) {
				long lOrder = tCurrent.getOrder();

				if (lOrder == 0) {
					String sFilename = fCurrent.getName();
					if (!Character.isDigit(sFilename.charAt(0))) {
						sErrors += fCurrent.getAbsolutePath() + " ("
								+ Messages.getString("Error.152") + ")\n";
						continue;
					} else {
						String sTo = fCurrent.getName().substring(0, 3).trim()
								.replaceAll("[^0-9]", "");
						for (char c : sTo.toCharArray()) {
							if (!Character.isDigit(c)) {
								sErrors += fCurrent.getAbsolutePath() + " ("
										+ Messages.getString("Error.152")
										+ ")\n";
								continue;
							}
						}
						lOrder = Long.parseLong(sTo);
					}
				}
				if (lOrder < 10) {
					filename = filename.replace(PATTERN_TRACKORDER, "0"
							+ lOrder);
				} else {
					filename = filename
							.replace(PATTERN_TRACKORDER, lOrder + "");
				}
			}

			// Check Track name
			if (filename.contains(PATTERN_TRACKNAME)) {

				sValue = tCurrent.getName().replace("/", "-");

				if (!sValue.equalsIgnoreCase(Messages.getString("unknown"))) {
					filename = filename.replace(PATTERN_TRACKNAME,
							AuthorManager.format(sValue));
				} else {
					sErrors += fCurrent.getAbsolutePath() + " ("
							+ Messages.getString("Error.151") + ")\n";
					continue;
				}
			}
			// Check Year Value
			if (filename.contains(PATTERN_YEAR)) {
				if (tCurrent.getYear() != 0) {
					filename = filename.replace(PATTERN_YEAR, tCurrent
							.getYear()
							+ "");
				} else {
					sErrors += fCurrent.getAbsolutePath() + " ("
							+ Messages.getString("Error.148") + ")\n";
					continue;
				}

			}

			filename += "." + tCurrent.getType().getExtension();

			// Compute the new filename
			java.io.File fOld = fCurrent.getIO();
			String sRoot = fCurrent.getDevice().getUrl();
			String[] split = filename.split("\\" + java.io.File.separator);
			String sName = split[0];

			// Check if directories exists, and if not create them
			String sPathname = getCheckedPath(sRoot, filename);
			java.io.File fNew = new java.io.File(sPathname);
			fNew.getParentFile().mkdirs();

			// Move file and related cover but save old Directory pathname for
			// futur deletion
			java.io.File fCover = tCurrent.getAlbum().getCoverFile();
			if (fCover != null) {
				fCover.renameTo(new java.io.File(fNew.getParent() + "/"
						+ fCover.getName()));
			}
			boolean bState = false;

			if (fNew.getAbsolutePath().equalsIgnoreCase(fOld.getAbsolutePath())) {
				sErrors += fCurrent.getAbsolutePath() + " ("
						+ Messages.getString("Error.160") + ")\n";
			} else {
				if (fNew.getParentFile().canWrite()) {
					bState = fOld.renameTo(fNew);
					if (!bState) {
						sErrors += fCurrent.getAbsolutePath() + " ("
								+ Messages.getString("Error.154") + ")\n";
					}
					Log.debug("[Refactoring] " + fNew.getAbsolutePath()
							+ " Success ? " + bState);

				} else {
					sErrors += fCurrent.getAbsolutePath() + " ("
							+ Messages.getString("Error.161") + ")\n";
				}
			}
			fCurrent.getDevice().cleanRemovedFiles();

			// Register and scans new directories
			Directory dir = DirectoryManager.getInstance().registerDirectory(
					sName,
					DirectoryManager.getInstance().getDirectoryForIO(
							fCurrent.getDevice().getFio()),
					fCurrent.getDevice());

			registerFile(dir);

			// See if old directory contain other files and move them
			java.io.File dOld = fOld.getParentFile();
			java.io.File[] list = dOld.listFiles(new JajukFileFilter(false));
			if (list == null) {
				DirectoryManager.getInstance()
						.removeDirectory(fOld.getParent());
			} else if (list.length != 0) {
				for (java.io.File f : list) {
					f.renameTo(new java.io.File(fNew.getParent() + "/"
							+ f.getName()));
				}
			} else if (list.length == 0) {
				if (dOld.delete()) {
					DirectoryManager.getInstance().removeDirectory(
							fOld.getParent());

				}
			}
		}

		if (!sErrors.equals("")) {
			Messages.showDetailedErrorMessage("147", "", sErrors);
		} else {
			InformationJPanel
					.getInstance()
					.setMessage(
							Messages.getString("Success"), InformationJPanel.INFORMATIVE); //$NON-NLS-1$
		}
	}

	public String getCheckedPath(String sRoot, String sPathname) {

		java.io.File fioRoot = new java.io.File(sRoot);
		java.io.File[] fioList = fioRoot.listFiles(new JajukFileFilter(true,
				false));

		String[] sPaths = sPathname.split("\\" + java.io.File.separator);
		String sReturn = sRoot;
		for (int i = 0; i < sPaths.length - 1; i++) {
			String sPath = sPaths[i];
			boolean bool = false;
			for (java.io.File fio : fioList) {
				String s = fio.getPath();
				if (s.equalsIgnoreCase(sReturn + sPath)) {
					sReturn += "/" + s.replace(sReturn, "");
					bool = true;
				}
			}
			if (bool == false) {
				sReturn += "/" + sPath;
			}
		}
		return sReturn + "/" + sPaths[sPaths.length - 1];
	}

	public void registerFile(Directory d) {

		java.io.File fList[] = d.getFio().listFiles(
				new JajukFileFilter(true, false));

		if (fList.length != 0) {
			for (java.io.File f : fList) {
				Directory dir = DirectoryManager.getInstance()
						.registerDirectory(f.getName(), d, d.getDevice());
				registerFile(dir);
			}
		} else {
			d.scan(true);
		}
	}
}
