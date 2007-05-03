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
 *  $$Revision$$
 */

package org.jajuk.util;

import org.jajuk.Main;
import org.jajuk.dj.AmbienceManager;

import java.io.File;

/**
 * Maintain all behavior needed upgrades from releases to releases
 */
public class UpgradeManager implements ITechnicalStrings {
	/**
	 * Actions to migrate an existing installation Step1 just at startup
	 * 
	 */
	public static void upgradeStep1() throws Exception {
		// --For jajuk < 0.2 : remove backup file : collection~.xml
		File file = Util.getConfFileByPath(FILE_COLLECTION + "~"); //$NON-NLS-1$
		file.delete();
		// upgrade code; if ugrade from <1.2, set default ambiences
		String sRelease = ConfigurationManager.getProperty(CONF_RELEASE);
		if (sRelease == null || sRelease.matches("0..*") //$NON-NLS-1$
				|| sRelease.matches("1.0..*") //$NON-NLS-1$
				|| sRelease.matches("1.1.*")) { //$NON-NLS-1$
			AmbienceManager.getInstance().createDefaultAmbiences();
		}
		// - For Jajuk < 1.3 : changed track pattern from %track to %title
		String sPattern = ConfigurationManager
				.getProperty(CONF_REFACTOR_PATTERN);
		if (sPattern.contains("track")) {
			ConfigurationManager.setProperty(CONF_REFACTOR_PATTERN, sPattern
					.replaceAll("track", "title"));
		}
		// - for Jajuk < 1.3: no more use of .ser files
		file = Util.getConfFileByPath("");
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			// delete all .ser files
			if (Util.getExtension(files[i]).equals("ser")) {
				files[i].delete();
			}
		}
		// - for jajuk 1.3: wrong option name: "false" instead of "jajuk.options.use_hotkeys"
		String sUseHotkeys = ConfigurationManager.getProperty("false");
		if (sUseHotkeys != null){
			if (sUseHotkeys.equalsIgnoreCase(FALSE) || sUseHotkeys.equalsIgnoreCase(TRUE)){
				ConfigurationManager.setProperty(CONF_OPTIONS_HOTKEYS,sUseHotkeys);
				ConfigurationManager.removeProperty("false");
			}
			else{
				ConfigurationManager.setProperty(CONF_OPTIONS_HOTKEYS, FALSE);
			}
		}
		// TO DO AFTER AN UPGRADE
		if (Main.isUpgradeDetected()) {
			// - for Jajuk < 1.3: force nocover icon replacement
			File fThumbs = Util.getConfFileByPath(FILE_THUMBS
					+ "/50x50/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
			if (fThumbs.exists()) {
				fThumbs.delete();
			}
			fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/100x100/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
			if (fThumbs.exists()) {
				fThumbs.delete();
			}
			fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/150x150/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
			if (fThumbs.exists()) {
				fThumbs.delete();
			}
			fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/200x200/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
			if (fThumbs.exists()) {
				fThumbs.delete();
			}
		}

	}

	/**
	 * Actions to migrate an existing installation Step 2 at the end of UI
	 * startup
	 */
	public static void upgradeStep2() throws Exception {

	}

	
}
