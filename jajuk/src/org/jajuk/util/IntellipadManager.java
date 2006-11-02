/*
 *  Jajuk
 *  Copyright (C) 2006 bflorat
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

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

/**
 * Type description
 * 
 * @author Bertrand Florat
 * @created 2 nov. 06
 */
public class IntellipadManager {

	private static JIntellitype jintellitype;

	/**
	 * Initialize intellipad hot keys
	 *
	 */
	public static void init() {
		jintellitype = new JIntellitype();
		// Assign global hotkeys to Windows+A and ALT+SHIFT+B
		jintellitype.registerHotKey(1, JIntellitype.MOD_WIN, 'A');
		jintellitype.registerHotKey(2, JIntellitype.MOD_ALT
				+ JIntellitype.MOD_SHIFT, 'B');
		// assign this class to be a HotKeyListener
		jintellitype.addHotKeyListener(new HotkeyListener() {

			// listen for hotkey
			public void onHotKey(int aIdentifier) {
				if (aIdentifier == 2)
					System.out.println("HERE !");
			}

		});

		// assign this class to be a IntellitypeListener
		jintellitype.addIntellitypeListener(new IntellitypeListener() {

			public void onIntellitype(int aCommand) {
				switch (aCommand) {
				case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
					System.out.println("Play/Pause message received "
							+ Integer.toString(aCommand));
					break;
				default:
					System.out.println("Undefined INTELLITYPE message caught "
							+ Integer.toString(aCommand));
					break;
				}
			}

		});
	}
	
	/**
	 * Free intellipad ressources
	 **/
	public static void cleanup(){
		jintellitype.cleanUp();
	}

}
