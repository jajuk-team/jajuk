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
 * Revision 1.3  2003/10/10 22:32:13  bflorat
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/09 21:14:13  bflorat
 * new keys and suppressed hard path
 *
 * Revision 1.1  2003/10/07 21:02:22  bflorat
 * Initial commit
 *
 */
package org.jajuk.base;

/**
 *  Contains all technical/ non-translatable strings
 *
 * @author     bflorat
 * @created    5 oct. 2003
 */
public interface TechnicalStrings {
	
		// directory path
	public static final String ICON_PATH = System.getProperty("user.dir")+"/dist-files/icons/";
	
		// paths to icons
	public static final String ICON_REPEAT = ICON_PATH + "16x16/repeat.png";
	public static final String ICON_SHUFFLE = ICON_PATH + "16x16/shuffle.png"; 
	public static final String ICON_CONTINUE = ICON_PATH + "16x16/continue.png";
	public static final String ICON_FILTER = ICON_PATH + "16x16/filter.png";
	public static final String ICON_ROLL = ICON_PATH + "16x16/roll.png";
	public static final String ICON_BESTOF = ICON_PATH + "16x16/bestof.png";
	public static final String ICON_MUTE = ICON_PATH + "16x16/mute.png";
	public static final String ICON_UP = ICON_PATH + "16x16/up.png";
	public static final String ICON_DOWN = ICON_PATH + "16x16/down.png";
	public static final String ICON_REW = ICON_PATH + "16x16/player_rew.png";
	public static final String ICON_PLAY = ICON_PATH + "16x16/player_end.png";
	public static final String ICON_STOP = ICON_PATH + "16x16/player_stop.png";
	public static final String ICON_FWD = ICON_PATH + "16x16/player_fwd.png";
	public static final String ICON_VOLUME = ICON_PATH + "16x16/volume.png";
	public static final String ICON_POSITION = ICON_PATH + "16x16/bottom.png";
	
	public static final String ICON_PERSPECTIVE_PHYSICAL			= ICON_PATH + "16x16/physical_perspective.png";
	public static final String ICON_PERSPECTIVE_LOGICAL			= ICON_PATH + "16x16/logical_perspective.png";
	public static final String ICON_PERSPECTIVE_HELP				= ICON_PATH + "16x16/info.png";
	public static final String ICON_PERSPECTIVE_STATISTICS		= ICON_PATH + "16x16/percent.png";
	public static final String ICON_PERSPECTIVE_CONFIGURATION	= ICON_PATH + "16x16/configure.png";

	//logs
	public static final String LOG_FILE = System.getProperty("user.home")+"/.jajuk/jajuk.log";
	public static final String LOG_PATTERN="%d{HH:mm:ss} [%p] %m\n";

}
