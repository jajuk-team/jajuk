/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Log$
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;

import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

/**
 *  abstract class for music player, independent from real implementation
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class Player {
	/** Implémentation class**/
	Object oImplPlayer;

	public Player(String sImplClass) throws Exception {
		oImplPlayer = Class.forName(sImplClass).newInstance();
	}

	public static void play(File file){
		try {
			IPlayerImpl playerImpl = file.getType().getPlayerImpl();
			playerImpl.play(file.getPath());
		} catch (Exception e) {
			Log.error(Messages.getString("Player.Error_playing____1") + file.getPath(), e); //$NON-NLS-1$
		}
	}

	public static void stop(Type type) {
		try {
				IPlayerImpl playerImpl = type.getPlayerImpl();
				playerImpl.stop();
			} catch (Exception e) {
				Log.error(Messages.getString("Player.Error_stoping____2") , e); //$NON-NLS-1$
			}
	}

	public static boolean isComplete(Type type) {
		return true;
	}

}
